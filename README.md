## Layout

- `model/`: accounts, donations, food items, tasks, events, lookup entities
- `controller/`: request-handling boundaries
- `service/`: business workflow orchestration
- `repository/`: JDBC persistence (only EventType is completed)
- `patterns/`: Phase 1 Factory, Strategy, Observer, Decorator
- `config/DatabaseManager.java`: Singleton
- `database/`: complete initial PostgreSQL schema and optional lookup seed data

`User` and `Donation` subclasses currently use simple inheritance and shared database tables with subtype tables. This is a proposed schema; verify it with your team before building all repositories. `ImpactReport` is computed and has no corresponding table.
