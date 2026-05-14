# Programmer Escrow Platform

This repository contains the MVP scaffold for a programmer escrow trading platform.

## Stack

- Backend: Spring Boot + MyBatis + MySQL + Redis
- Frontend: Vue 3 + Element Plus + Pinia + Vite
- Database schema: `sql/01_init_schema.sql`

## Repository Layout

- `backend/`: Spring Boot single application
- `frontend/`: Vue 3 application for client, developer, and admin portals
- `sql/`: MySQL DDL and seed data

## Current Scope

- Core database schema for demand, quote, order, chat, dispute, and admin flows
- Backend package skeleton for auth, demand, quote, order, dispute, and admin modules
- Frontend route skeleton for client, developer, and admin portals

## Startup Notes

1. Import `sql/01_init_schema.sql` into MySQL.
2. Update `backend/src/main/resources/application.yml` with your MySQL and Redis credentials.
3. Use JDK 17 or above to run the backend.
4. Start backend from `backend/`.
5. Install frontend dependencies in `frontend/` and run Vite.

## Bootstrap Admin

- Seed admin account: `admin`
- Bootstrap password: `admin123456`
- The current backend accepts this password when the seed hash remains the placeholder in `sql/01_init_schema.sql`.

## Important Environment Note

The current machine has both JDK 8 and JDK 17/21 installed. Maven is still bound to JDK 8 by default, so backend commands should be run with JDK 17+.
