{{#each versions}}
FROM ghcr.io/pssbletrngle/gradlehelper-docs:{{ this }} AS docs-{{ this }}
{{/each}}

FROM nginx:latest AS serve

ENV PORT=80

HEALTHCHECK --timeout=5s \
   CMD curl -s http://localhost || exit 1

COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY versions.json /usr/share/nginx/html/versions.json

{{#each versions}}
COPY --from=docs-{{ this }} /usr/share/nginx/html /usr/share/nginx/html/{{ this }}
{{/each}}