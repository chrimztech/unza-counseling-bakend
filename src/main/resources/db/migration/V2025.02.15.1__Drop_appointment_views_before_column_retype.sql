-- Drop views that depend on appointments.appointment_date before the
-- V2025.02.16 migration replaces the column to change its type.
DROP VIEW IF EXISTS counselor_workload;
DROP VIEW IF EXISTS client_dashboard;
