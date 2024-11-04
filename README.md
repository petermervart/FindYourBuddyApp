# FindYourBuddy Android App

**Authors:** Peter Mervart, Rastislav Kopča

This document provides an overview of a mobile application designed for Android 6.0 and above, focusing on connecting gamers to play together. The backend is developed using PostgreSQL and Django, while the frontend is implemented in Kotlin. Key features include user profiles, status updates, advertisements for finding game partners, and messaging, including video calls. Below is a summary of the database schema, main functionalities, and backend API endpoints.

## Database Schema

1. **Users** - Stores user information, including name, password, email, profile description, profile photo, and account creation time. Users can update their profile details.
2. **Statuses** - Allows users to post statuses that are visible only to friends, such as sharing game achievements. Users can delete their own statuses.
3. **Advertisements** - Enables users to post public advertisements to find gaming partners, specifying the game, rank, and experience. Ads can be filtered based on game and rank.
4. **Games** - Stores a list of supported games for filtering advertisements. Each entry includes the game’s name and description.
5. **Ranks** - Represents ranks for each game, allowing players to specify their rank when creating an advertisement.
6. **Messages** - Stores text messages exchanged between users, with sender and receiver information and the time of message creation.
7. **Friend_requests** - Manages friend requests between users, storing sender and receiver information and the time of the request.
8. **Friends** - Maintains records of friendships between users, including the time the friendship was established.

## API Endpoints

- **User Management**
  - `POST /register` - Registers a new user with basic information (name, email, password hash).
  - `GET /login` - Authenticates a user and returns user details upon successful login.
  - `PUT /users` - Updates the user’s profile information.
  - `GET /users` - Retrieves information about a specific user.

- **Friend Management**
  - `POST /friend_requests` - Sends a friend request to another user.
  - `GET /friend_requests` - Fetches friend requests for the user.
  - `DELETE /friend_requests` - Deletes a specific friend request.
  - `POST /friends` - Adds a user as a friend.
  - `GET /friends` - Retrieves the list of friends for a user.
  - `DELETE /friends` - Removes a friend from the user’s friend list.

- **Messaging**
  - `POST /messages` - Sends a message between friends.
  - `GET /messages (latest conversations)` - Retrieves the user’s most recent conversations.
  - `GET /messages (conversation)` - Fetches the conversation history between two users.

- **Status Updates**
  - `POST /statuses` - Creates a new status for the user.
  - `GET /statuses` - Fetches statuses visible to the user (only from friends).
  - `DELETE /statuses` - Deletes a specific status.

- **Advertisements**
  - `POST /advertisements` - Creates a new advertisement to find gaming partners.
  - `GET /advertisements` - Retrieves all advertisements, with optional filters for game and rank.
  - `DELETE /advertisements` - Deletes a specific advertisement.

- **Additional Data**
  - `GET /ranks` - Retrieves rank information based on game.
  - `GET /games` - Lists all games available for filtering advertisements.

## Implementation Details

The backend utilizes a relational database structure via PostgreSQL, managed through Django. The mobile frontend is developed in Kotlin, leveraging WebRTC for video calls. A third-party WebRTC sample from GitHub (https://github.com/developerspace-samples/WebRTC-KotlinSample) is used to handle video call functionality.

- **API Documentation**: [SwaggerHub API Documentation](https://app.swaggerhub.com/apis/Rasto-K/MTAA/1.0.1)

## Frontend Highlights

The frontend application provides an intuitive interface allowing users to:
- View and edit their profiles.
- Post advertisements for gaming partners with optional filters.
- Manage friend lists and requests.
- Exchange messages and initiate video calls with friends.
