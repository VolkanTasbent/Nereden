export type AnalysisStatus = 'pending' | 'processing' | 'completed' | 'failed';

export interface AnalysisRequest {
  id: string;
  userId: string;
  imageUrl: string;
  status: AnalysisStatus;
  createdAt: string;
  completedAt: string | null;
  errorMessage?: string | null;
}

export interface AnalysisResult {
  id: string;
  requestId: string;
  matches: import('./product.types').ProductMatch;
  confidence: number;
  processingTimeMs: number;
  createdAt: string;
}
