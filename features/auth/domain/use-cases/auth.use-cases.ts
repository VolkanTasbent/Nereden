import type { AuthRepository, AuthCredentials, RegisterData } from '../repositories/auth.repository';
import type { User } from '@/types';

export class SignInUseCase {
  constructor(private readonly repository: AuthRepository) {}

  execute(credentials: AuthCredentials): Promise<User> {
    return this.repository.signIn(credentials);
  }
}

export class SignUpUseCase {
  constructor(private readonly repository: AuthRepository) {}

  execute(data: RegisterData): Promise<User> {
    return this.repository.signUp(data);
  }
}

export class SignOutUseCase {
  constructor(private readonly repository: AuthRepository) {}

  execute(): Promise<void> {
    return this.repository.signOut();
  }
}

export class GetCurrentUserUseCase {
  constructor(private readonly repository: AuthRepository) {}

  execute(): Promise<User | null> {
    return this.repository.getCurrentUser();
  }
}
