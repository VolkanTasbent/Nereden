export class AppError extends Error {
  readonly code: string;
  readonly statusCode: number;
  readonly details?: Record<string, string[]>;

  constructor(
    message: string,
    code = 'UNKNOWN_ERROR',
    statusCode = 500,
    details?: Record<string, string[]>,
  ) {
    super(message);
    this.name = 'AppError';
    this.code = code;
    this.statusCode = statusCode;
    if (details !== undefined) {
      this.details = details;
    }
  }
}

export class NetworkError extends AppError {
  constructor(message = 'Ağ bağlantısı hatası. Lütfen tekrar deneyin.') {
    super(message, 'NETWORK_ERROR', 0);
    this.name = 'NetworkError';
  }
}

export class AuthError extends AppError {
  constructor(message = 'Oturum doğrulanamadı.', code = 'AUTH_ERROR') {
    super(message, code, 401);
    this.name = 'AuthError';
  }
}

export class ValidationError extends AppError {
  constructor(message: string, details?: Record<string, string[]>) {
    super(message, 'VALIDATION_ERROR', 422, details);
    this.name = 'ValidationError';
  }
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof AppError) {
    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'Beklenmeyen bir hata oluştu.';
}
