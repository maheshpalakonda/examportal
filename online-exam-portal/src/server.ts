import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import http from 'node:http';
import { URL as NodeURL } from 'node:url';

const serverDistFolder = dirname(fileURLToPath(import.meta.url));
const browserDistFolder = resolve(serverDistFolder, '../browser');

const app = express();
const angularApp = new AngularNodeAppEngine();

/**
 * Example Express Rest API endpoints can be defined here.
 * Uncomment and define endpoints as necessary.
 *
 * Example:
 * app.get('/api/**', (req, res) => {
 *   // Handle API request
 * });
 */

/**
 * Simple API proxy to forward "/api" requests to the Spring Boot backend
 * This avoids SSR returning the Angular HTML for API calls and fixes JSON parse errors.
 * Target can be overridden via env var API_TARGET.
 */
const API_TARGET = process.env['API_TARGET'] || 'http://72.60.219.208:8080';
app.use('/api', (req, res) => {
  const targetUrl = new NodeURL(req.originalUrl, API_TARGET);
  const backendHost = new NodeURL(API_TARGET).host;

  const proxyReq = http.request(targetUrl, {
    method: req.method,
    headers: { ...req.headers, host: backendHost },
  }, (proxyRes) => {
    res.status(proxyRes.statusCode || 502);
    for (const [key, value] of Object.entries(proxyRes.headers)) {
      if (value !== undefined) {
        // Types for header values can be string | string[] | number
        // Express accepts string | string[] for setHeader
        res.setHeader(key, value as any);
      }
    }
    proxyRes.pipe(res, { end: true });
  });

  proxyReq.on('error', (err) => {
    console.error('API proxy error:', err);
    res.status(502).send('Bad Gateway');
  });

  if (req.readable) {
    req.pipe(proxyReq, { end: true });
  } else {
    proxyReq.end();
  }
});

/**
 * Serve static files from /browser
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use('/**', (req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Start the server if this module is the main entry point.
 * The server listens on the port defined by the `PORT` environment variable, or defaults to 4000.
 */
if (isMainModule(import.meta.url)) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, () => {
    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);
