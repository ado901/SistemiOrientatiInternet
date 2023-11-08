# SOI 2023 - Docker project

## WSL Port Forwarding

In order to reach the Docker Compose port inside WSL from the LAN,
you have to type in the Windows terminal as Administrator
```
netsh interface portproxy set v4tov4 listenport=80 listenaddress=0.0.0.0 connectport=80 connectaddress=$(wsl hostname -I)
```

You then have to type
```
netsh interface portproxy delete v4tov4 listenport=80 listenaddress=0.0.0.0
```
in order to remove the port forwarding.
