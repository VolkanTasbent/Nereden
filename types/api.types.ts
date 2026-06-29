export interface ApiError {
  code: string;
  message: string;
  statusCode: number;
  details?: Record<string, string[]>;
}

export interface ApiResponse<T> {
  data: T;
  meta?: {
    page?: number;
    limit?: number;
    total?: number;
  };
}

export interface PaginatedParams {
  page?: number;
  limit?: number;
}
