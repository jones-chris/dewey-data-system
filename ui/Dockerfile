FROM node:18.14.0-alpine3.17

# Declare workind directory so that `npm install` will work.
WORKDIR /usr/app

# Copy source code files.
COPY ./connectors /usr/app/connectors
COPY ./core/build /usr/app/core
COPY ./index.js /usr/app

# Copy package.json and install node dependencies.
COPY ./package.json /usr/app
RUN npm install

EXPOSE 8081
USER node
CMD node /usr/app/index.js