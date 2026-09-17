# API Usage and Rate Limits

AcmeFlow provides a REST API for projects, tasks, comments, and members. API keys are created under **Settings > Developer > API keys** and inherit the permissions of the member who created them. Keys are shown only once and should never be embedded in client-side applications or committed to source control.

Rate limits apply to the whole workspace, not to each key. Free workspaces receive 100 requests per rolling hour, while Pro workspaces receive 5,000 requests per rolling hour. A response includes `X-RateLimit-Limit`, `X-RateLimit-Remaining`, and `X-RateLimit-Reset` headers.

When the limit is exceeded, the API returns HTTP status `429 Too Many Requests`. Clients should wait for the time indicated by the `Retry-After` header and retry with exponential backoff. Repeated immediate retries can delay recovery and should be avoided.

Successful and failed requests both count toward the limit, except health checks and workspace data exports. AcmeFlow may temporarily reduce limits during abuse or unusual traffic. Pro customers who consistently need additional capacity should contact priority support with expected request volume and usage patterns.