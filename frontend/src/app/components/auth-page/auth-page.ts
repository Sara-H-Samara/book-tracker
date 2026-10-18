import { Component, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { form, FormField, required, email, minLength } from '@angular/forms/signals';
import { AuthService } from '../../services/auth-service';

type AuthMode = 'login' | 'signup';

@Component({
  selector: 'app-auth-page',
  imports: [FormField, RouterLink],
  templateUrl: './auth-page.html',
  styleUrl: './auth-page.css',
})
export class AuthPage {
  private auth = inject(AuthService);
  private router = inject(Router);

  /** From the route's data: { mode: 'login' | 'signup' } */
  mode = input<AuthMode>('login');

  /** From the URL: /login?returnUrl=/books/2 */
  returnUrl = input<string>();

  isSignup = computed(() => this.mode() === 'signup');

  model = signal({ name: '', email: '', password: '' });

  authForm = form(this.model, (path) => {
    required(path.name, { message: 'Your name is required.', when: () => this.isSignup() });
    required(path.email, { message: 'Email is required.' });
    email(path.email, { message: 'Enter a valid email address.' });
    required(path.password, { message: 'Password is required.' });
    minLength(path.password, 8, { message: 'At least 8 characters.' });
  });

  submitting = signal(false);
  serverError = signal<string | null>(null);
  showPassword = signal(false);

  submit(event: Event) {
    event.preventDefault();

    if (this.authForm().invalid()) {
      this.authForm.name().markAsTouched();
      this.authForm.email().markAsTouched();
      this.authForm.password().markAsTouched();
      return;
    }

    const value = this.model();
    this.submitting.set(true);
    this.serverError.set(null);

    const request$ = this.isSignup()
      ? this.auth.signup(value.name.trim(), value.email.trim(), value.password)
      : this.auth.login(value.email.trim(), value.password);

    request$.subscribe({
      next: () => this.router.navigateByUrl(this.returnUrl() || '/'),
      error: (err: Error) => {
        this.serverError.set(err.message);
        this.submitting.set(false);
      },
    });
  }
}
