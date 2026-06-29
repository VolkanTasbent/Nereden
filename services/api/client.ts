import { AppError, AuthError, NetworkError } from '@/utils/errors';
import type { ApiError, ApiResponse } from '@/types';
import { appConfig } from '@/constants/config';
import { getAccessToken } from '@/utils/tokenStorage';

import { clearSession, tryRefreshTokens } from './tokenRefresh';

const DEFAULT_TIMEOUT_MS = 30_000;

interface RequestConfig extends Omit<RequestInit, 'body'> {
  body?: unknown;
  timeoutMs?: number;
  auth?: boolean;
}

export interface PaginatedResponse<T> {
  data: T;
  meta?: ApiResponse<T>['meta'];
}

export class ApiClient {
  private readonly baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl.replace(/\/$/, '');
  }

  async request<T>(endpoint: string, config: RequestConfig = {}): Promise<T> {
    const result = await this.requestRaw<T>(endpoint, config, false);
    return result.data;
  }

  async requestPaginated<T>(endpoint: string, config: RequestConfig = {}): Promise<PaginatedResponse<T>> {
    return this.requestRaw<T>(endpoint, config, false);
  }

  private async requestRaw<T>(
    endpoint: string,
    config: RequestConfig = {},
    isRetry = false,
  ): Promise<PaginatedResponse<T>> {
    const { body, timeoutMs = DEFAULT_TIMEOUT_MS, headers, auth = false, ...rest } = config;
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const isFormData = body instanceof FormData;
      const requestHeaders: Record<string, string> = isFormData
        ? { ...(headers as Record<string, string> | undefined) }
        : { 'Content-Type': 'application/json', ...(headers as Record<string, string> | undefined) };

      if (auth) {
        const token = await getAccessToken();
        if (token) {
          requestHeaders.Authorization = `Bearer ${token}`;
        }
      }

      const init: RequestInit = {
        ...rest,
        headers: requestHeaders,
        signal: controller.signal,
      };

      if (body !== undefined) {
        init.body = isFormData ? body : JSON.stringify(body);
      }

      const response = await fetch(`${this.baseUrl}${endpoint}`, init);

      if (!response.ok) {
        if (auth && (response.status === 401 || response.status === 403) && !isRetry) {
          const refreshed = await tryRefreshTokens();
          if (refreshed) {
            return this.requestRaw<T>(endpoint, config, true);
          }

          await clearSession();
          throw new AuthError('Oturumunuz sona erdi. Lütfen tekrar giriş yapın.');
        }

        const errorBody = (await response.json().catch(() => null)) as ApiError | null;
        throw new AppError(
          errorBody?.message ?? 'İstek başarısız oldu.',
          errorBody?.code ?? 'API_ERROR',
          response.status,
          errorBody?.details,
        );
      }

      const payload = (await response.json()) as ApiResponse<T> | T;

      if (payload !== null && typeof payload === 'object' && 'data' in payload) {
        const wrapped = payload as ApiResponse<T>;
        return { data: wrapped.data, meta: wrapped.meta };
      }

      return { data: payload as T };
    } catch (error) {
      if (error instanceof AppError) {
        throw error;
      }

      if (error instanceof Error && error.name === 'AbortError') {
        throw new NetworkError('İstek zaman aşımına uğradı.');
      }

      throw new NetworkError();
    } finally {
      clearTimeout(timeoutId);
    }
  }
}

export const aiApiClient = new ApiClient(appConfig.aiApiUrl);
