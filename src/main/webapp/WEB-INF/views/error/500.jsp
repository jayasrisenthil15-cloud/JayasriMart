<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 - Server Error | JayasriMart</title>
    <style>
        :root {
            --primary: #2563eb;
            --text: #1e293b;
            --bg: #f8fafc;
            --card-bg: #ffffff;
            --border: #e2e8f0;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg);
            color: var(--text);
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            margin: 0;
            padding: 1rem;
        }
        .error-card {
            background: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 2.5rem;
            max-width: 480px;
            text-align: center;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
        }
        .code {
            font-size: 4rem;
            font-weight: 800;
            color: #ef4444;
            margin: 0;
            line-height: 1;
        }
        h1 {
            font-size: 1.5rem;
            margin: 1rem 0 0.5rem;
        }
        p {
            color: #64748b;
            margin-bottom: 2rem;
            line-height: 1.5;
        }
        .btn {
            display: inline-block;
            background-color: var(--primary);
            color: white;
            padding: 0.75rem 1.5rem;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 500;
            transition: background 0.2s;
        }
        .btn:hover {
            background-color: #1d4ed8;
        }
    </style>
</head>
<body>
    <div class="error-card">
        <div class="code">500</div>
        <h1>Internal Server Error</h1>
        <p>Something went wrong on our servers. Our engineering team has been notified. Please try again in a few moments.</p>
        <a href="${pageContext.request.contextPath}/" class="btn">Return to Home</a>
    </div>
</body>
</html>
