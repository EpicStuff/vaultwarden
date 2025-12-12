# HTTP unlock error analysis

## What happened
The log captured while using the HTTP base URL shows the login token response omits the `userDecryptionOptions.masterPasswordUnlock` block:

```
"UserDecryptionOptions":{"HasMasterPassword":true,"Object":"userDecryptionOptions"},
```

Because that structure never contains the `masterPasswordUnlock` payload, the app cannot build the data needed for Master Password unlock and eventually throws `MissingPropertyException("MasterPasswordUnlock data")` when you try to unlock.

## Why HTTPS works
When you sign in over HTTPS against a server that returns the full `userDecryptionOptions` payload (including `masterPasswordUnlock`), the app caches that data during login and can unlock the vault later. The HTTP server you hit is returning a trimmed payload, so the unlock prerequisites are never stored.

## How to fix
Use a server that returns `userDecryptionOptions.masterPasswordUnlock` (e.g., enable HTTPS on your instance or update the server so it emits the full payload for HTTP). Once that field is present in the login/sync responses, the unlock flow works the same way as it does over HTTPS.
