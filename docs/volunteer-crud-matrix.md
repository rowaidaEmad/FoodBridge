# CRUD Matrix: Volunteer Module

Each row is a user requirement, and each column is an entity. C = create, R = read, U = update, D = delete.

| Requirement | Volunteer | PickupTask | Event | EventRegistration | VolunteerHours | Certificate | Distribution | Notification |
|---|---|---|---|---|---|---|---|---|
| Register as a volunteer | C | | | | | | | |
| Update skills, availability and preferences | RU | | | | | | | |
| View and accept or decline tasks | R | RU | | | | | | |
| Mark task picked up | | RU | | | | | | |
| Mark task delivered | U | RU | | | C | | C | |
| Admin ranks and assigns to a task | R | RU | | | | | | C |
| Register for an event | R | | R | CR | | | | |
| Mark event attendance | U | | R | RU | C | | | |
| Announce an event update | | | R | R | | | | C |
| Log and view hours | RU | | | | CR | | | |
| Request a certificate | R | | | | R | CR | | |
| View notifications and mark as read | | | | | | | | RU |

There are no D entries because this module keeps its records as history, and deactivating a user is an update.
