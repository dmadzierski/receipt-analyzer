FROM node:22.17.0

WORKDIR /usr/src/app

COPY package.json package-lock.json ./

RUN npm install

COPY . .

RUN npx ng build --configuration=production

EXPOSE 4200



CMD npm run start-prod
