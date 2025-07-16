local typedefs = require "kong.db.schema.typedefs"

return {
  name = "custom-auth",
  fields = {
    { consumer = typedefs.no_consumer },
    { protocols = typedefs.protocols_http },
    { config = {
        type = "record",
        fields = {
          { auth_service_url = { type = "string", required = true, default = "http://auth-service:9811/auth/v1/ping" } },
          { timeout = { type = "number", default = 5000 } },
          { retries = { type = "number", default = 3 } },
        },
      },
    },
  },
}