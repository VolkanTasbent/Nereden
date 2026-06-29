import { z } from 'zod';

export const emailSchema = z
  .string()
  .min(1, 'E-posta adresi gereklidir.')
  .email('Geçerli bir e-posta adresi girin.');

export const passwordSchema = z
  .string()
  .min(8, 'Şifre en az 8 karakter olmalıdır.')
  .regex(/[A-Z]/, 'Şifre en az bir büyük harf içermelidir.')
  .regex(/[0-9]/, 'Şifre en az bir rakam içermelidir.');

export const loginSchema = z.object({
  email: emailSchema,
  password: z.string().min(1, 'Şifre gereklidir.'),
});

export const registerSchema = z
  .object({
    fullName: z.string().min(2, 'Ad soyad en az 2 karakter olmalıdır.'),
    email: emailSchema,
    password: passwordSchema,
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Şifreler eşleşmiyor.',
    path: ['confirmPassword'],
  });

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
