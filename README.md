<div align="center">

# 📚 Book Tracker

**A reading tracker for the books you want to read, are reading, and have finished.**

![Angular](https://img.shields.io/badge/Angular-22-DD0031?logo=angular&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6?logo=typescript&logoColor=white)
![Signals](https://img.shields.io/badge/State-Signals-8A5A3B)
![License](https://img.shields.io/badge/License-MIT-3F8A68)

![My shelf](docs/screenshots/shelf.png)

</div>

## Table of contents

- [About](#about)
- [Features](#features)
- [Screenshots](#screenshots)
- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Project structure](#project-structure)
- [Architecture notes](#architecture-notes)
- [Current limitations](#current-limitations)
- [Roadmap](#roadmap)
- [Author](#author)
- [License](#license)

## About

Book Tracker keeps your reading life on one shelf. Search for a book through the
[Open Library API](https://openlibrary.org/developers/api) to fill in its title, author, year and cover,
or type it in yourself. Then move it from _To read_ → _Reading_ → _Finished_.

The design is inspired by **library index cards**: ruled blue lines, a red top rule,
rubber-stamp status labels and a little bookshelf in the masthead.

## Features

- **Shelf** with filter tabs (All · Reading · To read · Finished) and live counts
- **Three reading statuses** with one-click progress: _Start reading → Mark finished → Read again_
- **Add books** by searching Open Library or by hand, with validation
- **Edit books**: the same form works in _add_ and _edit_ mode
- **Book details** page with a large cover and a status picker
- **Delete** with an inline confirmation step
- **Login and sign-up** with validation, protected routes and redirect back after login
- **Responsive** layout and accessibility basics: labels, `aria-*` attributes, visible keyboard focus

## Screenshots

|                  Add a book                  |                 Book details                  |                Login                 |
| :------------------------------------------: | :-------------------------------------------: | :----------------------------------: |
| ![Add a book](docs/screenshots/add-book.png) | ![Book details](docs/screenshots/details.png) | ![Login](docs/screenshots/login.png) |

## Tech stack

| Area      | Technology                                                                          |
| --------- | ----------------------------------------------------------------------------------- |
| Framework | Angular 22: standalone components, built-in control flow (`@if`, `@for`, `@switch`) |
| State     | Signals: `signal`, `computed`, signal inputs                                        |
| Forms     | Signal Forms: `form()`, `FormField`, built-in and conditional validators            |
| Routing   | Angular Router: route parameters, component input binding, functional guards        |
| HTTP      | `HttpClient` + RxJS for the Open Library API                                        |
| Language  | TypeScript: union types, `Record`, `Partial`, `Omit`                                |
| Styling   | Plain CSS: custom properties (design tokens), Grid, Flexbox                         |

## Getting started

### Prerequisites

- [Node.js](https://nodejs.org/) and npm (current LTS)
- [Angular CLI](https://angular.dev/tools/cli): `npm install -g @angular/cli`

### Installation

```bash
git clone https://github.com/Sara-H-Samara/book-tracker.git
cd book-tracker
npm install
```

### Run

```bash
ng serve
```

Open **http://localhost:4200**, create an account, and start adding books.

## Project structure

```
src/app/
├── components/
│   ├── auth-page/      # Login and sign-up (one component, two modes)
│   ├── book-details/   # Single book: cover, status picker, edit and remove
│   ├── book-form/      # Add and edit (one component, two modes) + Open Library search
│   ├── book-list/      # The shelf: filters, cards, inline delete confirmation
│   └── header/         # Masthead: title, decorative bookshelf, tabs, account
├── guards/             # authGuard, guestGuard
├── models/             # Book, BookStatus, User
├── services/           # BookService, OpenLibraryService, AuthService
├── app.config.ts       # Router, HttpClient and other providers
└── app.routes.ts       # Public and protected routes
```

## Architecture notes

- **Services own the data.** Components read signals from services and call service methods to change data.
  When the backend arrives, only the services change.
- **Read-only signals.** Services expose `asReadonly()` signals, so state can only change through service methods.
- **Immutable updates.** Lists are never mutated. Every change creates a new array (`map`, `filter`, spread).
- **One component, two modes.** The book form (add/edit) and the auth page (login/sign-up) are each reused
  across two routes, driven by route parameters and route data.
- **Design tokens.** All colors, fonts and spacing live as CSS custom properties in `styles.css`.

## Current limitations

This repository is the **frontend stage** of the project:

- Books are stored **in memory** and reset when the page is refreshed.
- Login uses a **temporary browser mock**. It is for development only and is **not secure**.

Both will be replaced by a Spring Boot backend (see the roadmap).

## Roadmap

- [x] Shelf, filters and reading statuses
- [x] Add, edit and delete books
- [x] Open Library search with covers
- [x] Login and sign-up pages with route guards
- [ ] Spring Boot REST API with a database
- [ ] Real authentication: hashed passwords, JWT, HTTP interceptor
- [ ] A separate shelf for each user
- [ ] Lazy-loaded routes, unit tests and a production build

## Author

**Sara Samara**, [GitHub @Sara-H-Samara](https://github.com/Sara-H-Samara)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

Book data and covers are provided by [Open Library](https://openlibrary.org/).
