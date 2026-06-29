import type { AnalysisRequest, AnalysisResult, Product, ProductMatch } from '@/types';
import { appConfig } from '@/constants/config';
import { ApiClient } from '@/services/api/client';
import { sortStoresByPrice } from '@/utils/stores';

interface ApiAnalysisRequest {
  id: string;
  userId: string;
  imageUrl: string;
  status: string;
  createdAt: string;
  completedAt: string | null;
  errorMessage?: string | null;
}

interface ApiProductSummary {
  id: string;
  title: string;
  description: string | null;
  imageUrl: string;
  category: string;
  priceRange: { min: number; max: number; currency: string } | null;
  stores: Product['stores'];
  similarityScore: number | null;
  exactMatch: boolean;
  brand: string | null;
  createdAt: string;
}

interface ApiAnalysisResult {
  id: string;
  requestId: string;
  matches: {
    exactMatch: ApiProductSummary | null;
    similarProducts: ApiProductSummary[];
    cheaperAlternatives: ApiProductSummary[];
    estimatedPriceRange: { min: number; max: number; currency: string } | null;
  };
  confidence: number;
  processingTimeMs: number;
  createdAt: string;
}

function mapProduct(summary: ApiProductSummary): Product {
  return {
    id: summary.id,
    title: summary.title,
    description: summary.description,
    imageUrl: summary.imageUrl,
    category: summary.category as Product['category'],
    priceRange: summary.priceRange
      ? { min: Number(summary.priceRange.min), max: Number(summary.priceRange.max), currency: 'TRY' }
      : null,
    stores: sortStoresByPrice(
      summary.stores.map((s) => ({
        ...s,
        price: s.price !== null ? Number(s.price) : null,
        currency: 'TRY' as const,
      })),
    ),
    similarityScore: summary.similarityScore,
    isExactMatch: summary.exactMatch,
    brand: summary.brand,
    createdAt: summary.createdAt,
  };
}

function mapResult(raw: ApiAnalysisResult): AnalysisResult {
  const matches: ProductMatch = {
    exactMatch: raw.matches.exactMatch ? mapProduct(raw.matches.exactMatch) : null,
    similarProducts: raw.matches.similarProducts.map(mapProduct),
    cheaperAlternatives: raw.matches.cheaperAlternatives.map(mapProduct),
    estimatedPriceRange: raw.matches.estimatedPriceRange
      ? {
          min: Number(raw.matches.estimatedPriceRange.min),
          max: Number(raw.matches.estimatedPriceRange.max),
          currency: 'TRY',
        }
      : null,
  };

  return {
    id: raw.id,
    requestId: raw.requestId,
    matches,
    confidence: raw.confidence,
    processingTimeMs: raw.processingTimeMs,
    createdAt: raw.createdAt,
  };
}

export interface CreateAnalysisPayload {
  imageUrl: string;
}

export interface AiService {
  createAnalysisRequest(payload: CreateAnalysisPayload): Promise<AnalysisRequest>;
  getAnalysisResult(requestId: string): Promise<AnalysisResult>;
  getAnalysisStatus(requestId: string): Promise<AnalysisRequest>;
  retryAnalysis(requestId: string): Promise<AnalysisRequest>;
}

const apiClient = new ApiClient(appConfig.aiApiUrl);

function mapAnalysisRequest(raw: ApiAnalysisRequest): AnalysisRequest {
  return {
    id: raw.id,
    userId: raw.userId,
    imageUrl: raw.imageUrl,
    status: raw.status as AnalysisRequest['status'],
    createdAt: raw.createdAt,
    completedAt: raw.completedAt,
    errorMessage: raw.errorMessage ?? null,
  };
}

export const aiService: AiService = {
  async createAnalysisRequest(payload) {
    const response = await apiClient.request<ApiAnalysisRequest>('/analysis', {
      method: 'POST',
      auth: true,
      body: payload,
    });
    return mapAnalysisRequest(response);
  },

  async getAnalysisStatus(requestId) {
    const response = await apiClient.request<ApiAnalysisRequest>(`/analysis/${requestId}`, {
      auth: true,
    });
    return mapAnalysisRequest(response);
  },

  async getAnalysisResult(requestId) {
    const response = await apiClient.request<ApiAnalysisResult>(`/analysis/${requestId}/result`, {
      auth: true,
    });
    return mapResult(response);
  },

  async retryAnalysis(requestId) {
    const response = await apiClient.request<ApiAnalysisRequest>(`/analysis/${requestId}/retry`, {
      method: 'POST',
      auth: true,
    });
    return mapAnalysisRequest(response);
  },
};
