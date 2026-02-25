DELETE FROM users u
WHERE u.email ~ '^[a-z0-9_.-]+\.[a-z0-9_.-]+\.[0-9]{4}@example\.com$'
  AND u.phone_number LIKE '+7991%'
  AND u.year_of_admission IN (2025, 2026, 2027);