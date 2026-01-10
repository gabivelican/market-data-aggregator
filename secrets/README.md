# Secret files template - DO NOT COMMIT ACTUAL SECRETS
# Copy this file, rename to db_password.txt, jwt_secret.txt, api_key.txt and add actual values

# Database Password (for PostgreSQL)
# Example: 1q2w3e
[db_password_here]

# JWT Secret (for token generation and validation, should be at least 64 characters for HS512)
# Example: mySecretKeyForJWTTokenGenerationAndValidationShouldBeLongEnoughForHS512Algorithm
[jwt_secret_here]

# API Key (for internal C++ ↔ Gateway communication)
# Example: supersecret123-change-in-production
[api_key_here]
# Secrets directory should not be committed to version control
# Use this only for local development
*.txt

