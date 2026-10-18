import { Routes } from '@angular/router';
import { BookList } from './components/book-list/book-list';
import { BookForm } from './components/book-form/book-form';
import { BookDetails } from './components/book-details/book-details';
import { AuthPage } from './components/auth-page/auth-page';
import { authGuard, guestGuard } from './guards/auth-guard';

export const routes: Routes = [
  // ---------- Public: only for logged-out users ----------
  {
    path: 'login',
    component: AuthPage,
    canActivate: [guestGuard],
    data: { mode: 'login' },
    title: 'Log in · Book Tracker',
  },
  {
    path: 'signup',
    component: AuthPage,
    canActivate: [guestGuard],
    data: { mode: 'signup' },
    title: 'Sign up · Book Tracker',
  },

  // ---------- Private: only for logged-in users ----------
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: '', component: BookList, title: 'My Shelf · Book Tracker' },
      { path: 'add', component: BookForm, title: 'Add a Book · Book Tracker' },
      { path: 'books/:id', component: BookDetails, title: 'Book · Book Tracker' },
      { path: 'books/:id/edit', component: BookForm, title: 'Edit Book · Book Tracker' },
    ],
  },

  { path: '**', redirectTo: '' },
];
