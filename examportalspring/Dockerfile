FROM openjdk:21-jdk-slim

# Set environment variable for non-interactive install
ENV DEBIAN_FRONTEND=noninteractive
ENV APP_USER=nonrootuser
ENV APP_UID=1000

# 1. Install Python 3, GCC, and Build Utilities
# 'build-essential' is crucial for robust C/C++ compilation
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        python3 \
        gcc \
        build-essential \
    # 2. Create the non-root user with the specific UID
    && groupadd --gid $APP_UID $APP_USER && \
    useradd --uid $APP_UID --gid $APP_UID --shell /bin/bash --create-home $APP_USER \
    # 3. Clean up package lists to keep image size small
    && rm -rf /var/lib/apt/lists/*

# Set the working directory for code execution
WORKDIR /code

# Switch to the non-root user. This is the security sandbox layer.
USER $APP_USER

# Define the default command
CMD ["sh"]
