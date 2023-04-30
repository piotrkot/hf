
# Info

This is HF demo project.

# Setup

Create a local database in the docker container:
```
$> docker-compose up
$> psql -h localhost -p 5433 -U hfuser -d hfdb -f tbl-setup.sql
```

# Building

To build run the following command:
```
$> mvn clean package
```

# Running

To run the HF web application execute the following command:
```
$> java -cp target/classes:target/dependency/* com.github.piotrkot.hf.WebApp
```

The following environment variables need to be defined:

* `PORT=8080` - port number for server socket
* `THREADS=4` - number of threads running in parallel on the server
* `DATABASE_URL=postgres://hfuser:hfpass@localhost:5433/hfdb` - URL
to the local PostgreSQL database running in the docker container.

# Design decisions

* Web framework - Initially, I tried to use Spring, and JPA but the task
turned out to be more complicated. For that I decided to use
the web framework used recently in higly demanding environment.
Additionally, I already had a template (maven archetype) which
shortened the development time.
* Tools - Small and simple dependencies in the project. Small memory
footprint. Great flexibility as of native SQL commands, and tuning
of the REST support. Start-up faster and lighter than in Spring, Dropwizard
not mentioning JavaEE. Easily deployable in AWS, Azure, Heroku, and perhaps more.
No third-party dependencies, interoperability, no vendor lock-in.
* Code style - Out of habit, I followed the strict code analysis tools, like
checkstyle, findbugs, pmd, and more. The similar is with writing javadoc
* Tests - Unfortunately, I had no time to write unit tests. I always write
them and check test coverage regularly. But this time there was too high
time constraint pressure.
* DB - PostgreSQL is my favourite relational DBMS.

Yet, it is possible to implement the solution with a different tool set.
And I'm open to other approaches.

## API

I introduced a notion of a marking. This it to represent both bookings
and blocks.

### CRUD endpoints

It's possible to list a single booking or a block, all the bookings, all the blocks
or even all the markings.

It's possible to create a single booking or a block, edit single booking or a block,
and delete a single booking or a block. Ensuring no booking overlaps with other bookings and blocks,
and no block overlaps with other bookings. But it is possible to request a
block for an interval that is partially of fully occupied by another block.

The idea was that bookings are performed by tenants
and very likely a single tenant will not make more bookings at the same time.
Each such booking may
involve an invoice, a payment, an official agreement (contract). For that we may
not need to improve the standard process.
 
For the owner the situation may be a bit different. Owner may have many blocks,
may want to create some automatically, and rapidly change or reorganize them.
Dealing with multiple blocks one-by-one can be troublesome.

Like creating many short blocks that need to be merged into one long one. Or making a gap in
a long block. These operations can be improved.
Instead of changing single blocks we can add a long block on top of many few ones and
let the system merge the intervals. On the other hand, making a gap in a block
can be processed by removing a part-block from the long one and
let the system do the split.

Eventually, we want to have at most one block for any day in the system.
 
# Available services

## List markings

`GET /`

Lists all markings.

**Result**

Service returns HTTP 200 OK with body listing all markings on success,
and HTTP 400 Bad Request on failure.

**Example**

*Request*

`GET /`

*Response*

`200 OK`
```json
[
  {
    "id": 12,
    "start": "2022-01-22",
    "end": "2022-01-25",
    "category": "BOOKING"
  },
  {
    "id": 13,
    "start": "2022-02-22",
    "end": "2022-02-25",
    "category": "BLOCK"
  }
]
```

## List bookings

`GET /bookings`

Lists all bookings.

**Result**

Service returns HTTP 200 OK with body listing all bookings on success,
and HTTP 400 Bad Request on failure.

**Example**

*Request*

`GET /bookings`

*Response*

`200 OK`
```json
[
  {
    "id": 12,
    "start": "2022-01-22",
    "end": "2022-01-25",
    "category": "BOOKING"
  },
  {
    "id": 13,
    "start": "2022-02-22",
    "end": "2022-02-25",
    "category": "BOOKING"
  }
]
```

## List blocks

`GET /blocks`

Lists all blocks.

**Result**

Service returns HTTP 200 OK with body listing all blocks on success,
and HTTP 400 Bad Request on failure.

**Example**

*Request*

`GET /blocks`

*Response*

`200 OK`
```json
[
  {
    "id": 12,
    "start": "2022-01-22",
    "end": "2022-01-25",
    "category": "BLOCK"
  },
  {
    "id": 13,
    "start": "2022-02-22",
    "end": "2022-02-25",
    "category": "BLOCK"
  }
]
```

## List booking by id

`GET /bookings/<id>`

Lists booking by id.

**Result**

Service returns HTTP 200 OK with body listing the booking with given id on success,
and HTTP 400 Bad Request on failure.

**Example**

*Request*

`GET /bookings/12`

*Response*

`200 OK`
```json
[
  {
    "id": 12,
    "start": "2022-01-22",
    "end": "2022-01-25",
    "category": "BOOKING"
  }
]
```

## List block by id

`GET /blocks/<id>`

Lists block by id.

**Result**

Service returns HTTP 200 OK with body listing the block with given id on success,
and HTTP 400 Bad Request on failure.

**Example**

*Request*

`GET /blocks/12`

*Response*

`200 OK`
```json
[
  {
    "id": 12,
    "start": "2022-01-22",
    "end": "2022-01-25",
    "category": "BLOCK"
  }
]
```

## Create booking

`POST /bookings`

Request JSON body parameters:

* `start` - Start date (including) in `yyyy-MM-dd` format,
* `end` - End date (excluding) in `yyyy-MM-dd` format.

Creates new booking.

**Result**

Service returns HTTP 201 OK with body containing id of newly created booking on success,
and HTTP 400 Bad Request on failure.

**Example**

*Request*

`POST /bookings`
```json
{
  "start": "2022-01-22",
  "end": "2022-01-25"
}
```

*Response*

`201 OK`
```json
{
  "id": 12
}
```

## Create block

`POST /blocks`

Request JSON body parameters:

* `start` - Start date (including) in `yyyy-MM-dd` format,
* `end` - End date (excluding) in `yyyy-MM-dd` format.

Creates new block with given start date (including) and end date (excluding).
This operation may merge other blocks if they belong to the interval with
given start and end dates.

**Result**

Service returns HTTP 201 OK with body containing id of newly created block on success,
and HTTP 400 Bad Request on failure.

**Example**

*Request*

`POST /blocks`
```json
{
  "start": "2022-01-22",
  "end": "2022-01-25"
}
```

*Response*

`201 OK`
```json
{
  "id": 12
}
```

## Update booking

`PUT /bookings/<id>`

where `<id>` is identity of the booking to be updated.

Request JSON body parameters:

* `start` - Start date (including) in `yyyy-MM-dd` format,
* `end` - End date (excluding) in `yyyy-MM-dd` format.

Updates booking with given id.

**Result**

Service returns HTTP 200 OK on success,
and HTTP 400 Bad Request when update cannot be executed.

**Example**

*Request*

`PUT /bookings/1`
```json
{
  "start": "2022-01-22",
  "end": "2022-01-25"
}
```

*Response*

`200 OK`

## Update block

`PUT /blocks/<id>`

where `<id>` is identity of the block to be updated.

Request JSON body parameters:

* `start` - Start date (including) in `yyyy-MM-dd` format,
* `end` - End date (excluding) in `yyyy-MM-dd` format.

Updates block with given id.

**Result**

Service returns HTTP 200 OK on success,
and HTTP 400 Bad Request when update cannot be executed.

**Example**

*Request*

`PUT /blocks/1`
```json
{
  "start": "2022-01-22",
  "end": "2022-01-25"
}
```

*Response*

`200 OK`

## Delete booking by id

`DELETE /bookings/<id>`

where `<id>` is identity of the booking to be deleted.

Deletes booking with given id.

**Result**

Service returns HTTP 204 No Content on success.

**Example**

*Request*

`DELETE /bookings/1`

*Response*

`204 No Content`

## Delete block by id

`DELETE /blocks/<id>`

where `<id>` is identity of the block to be deleted.

Deletes block with given id.

**Result**

Service returns HTTP 204 No Content on success.

**Example**

*Request*

`DELETE /blocks/1`

*Response*

`204 No Content`

## Delete block by range

`DELETE /blocks?start=<start>&end=<end>`

where:
* `<start>` - Start date (including) in `yyyy-MM-dd` format,
* `<end>` - End date (excluding) in `yyyy-MM-dd` format.

Deletes block or blocks that belong to the interval with given start date (including)
and end date (excluding).

**Result**

Service returns HTTP 204 No Content on success.

**Example**

*Request*

`DELETE /blocks?start=2020-01-05&end=2020-02-05`

*Response*

`204 No Content`
