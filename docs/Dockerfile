FROM squidfunk/mkdocs-material:latest AS builder

ENV CI=true

# install dependencies
COPY docs/requirements.txt .
RUN pip install -r requirements.txt

# build the project
ARG repo_url
ENV REPO_URL=$repo_url

ARG site_url
ENV SITE_URL=$site_url

ARG edit_url
ENV EDIT_URL=$edit_url

COPY docs/src src
COPY docs/mkdocs.yml .
COPY .git .git
RUN mkdocs build

FROM nginx:latest AS serve

EXPOSE 80

HEALTHCHECK --timeout=5s \
   CMD curl -s http://localhost || exit 1

COPY docs/nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /docs/site /usr/share/nginx/html