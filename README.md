This Android project demonstrates how to build a weather application using Jetpack Compose, weather data retrieval from a weather API and SQL to store the data. Key features include:

Date Selection: User-friendly DatePicker component to select a desired date.

City Selection: Predefined list of cities the user can choose from (saved longitude and latitude)

Weather Retrieval: Fetches past or temperature data from an external weather API [https://archive-api.open-meteo.com/v1/archive] and retrofit for future dates, past 10 years data for the same date is averaged and returned to user.

Data Caching: Room database to store weather data locally, minimizing redundant API requests.

Obtaining Future Weather Insights Using Data from the Last 10 Years

UI: Jetpack Compose

Network: Retrofit

Database: SQLite
