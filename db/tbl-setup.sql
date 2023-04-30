-- Category of booking
CREATE TYPE cat AS ENUM ('BOOKING', 'BLOCK');

CREATE TABLE bookings(
  -- Identity
  id_i serial PRIMARY KEY,
  -- Start date inclusive
  start_d DATE NOT NULL,
  -- End date exclusive
  end_d DATE NOT NULL CHECK (end_d > start_d),
  -- Booking category
  book_cat cat
);
