# Water Consumption Tracker - Requirements Document

## Overview
The Water Consumption Tracker is a web application designed to monitor water usage across multiple locations. It provides data visualization through tables, pie charts, bar charts, and line graphs, allowing users to track and analyze their water consumption patterns.

## User Types
- **Client**: The only user type in the system. Clients can add, edit, and delete water consumption data.

## Features
1. **User Authentication**
   - Login with email and password
   - No registration functionality required (users are pre-configured)

2. **Water Consumption Data Management**
   - Add new consumption data entries via a modal form
   - Edit existing entries through a popup modal
   - Delete entries
   - View all entries in a table format

3. **Data Visualization**
   - Table view of all consumption data
   - Pie chart visualization
   - Bar chart visualization
   - Line graph visualization

4. **Location Management**
   - Track water consumption across multiple locations
   - Filter data by location

## Technical Specifications
- **Backend**: Spring Boot with Controller-Service architecture
- **Frontend**: Thymeleaf for server-side rendering
- **Database**: H2 in-memory database for development
- **Build Tool**: Gradle
- **Security**: Spring Security for authentication
- **Data Visualization**: JavaScript libraries (Chart.js)

## Data Model
1. **User**
   - id (Long)
   - email (String)
   - password (String)
   - name (String)

2. **Location**
   - id (Long)
   - name (String)
   - address (String)
   - userId (Long)

3. **WaterConsumption**
   - id (Long)
   - locationId (Long)
   - amount (Double)
   - unit (String)
   - date (LocalDate)
   - notes (String)

## User Interface
1. **Login Page**
   - Email and password fields
   - Login button

2. **Dashboard**
   - Summary of water consumption
   - Quick access to add new entries
   - Navigation to detailed views

3. **Data Entry View**
   - Table of all entries with options to edit/delete
   - Button to add new entries (opens modal)
   - Modal form for adding/editing entries

4. **Visualization View**
   - Toggle between different visualization types
   - Filter options for date range and locations
