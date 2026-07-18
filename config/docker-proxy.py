#!/usr/bin/env python3
import socket, threading, os, signal, sys, re, datetime

PROXY_SOCKET = "/tmp/docker-proxy.sock"
DOCKER_SOCKET = "/var/run/docker.sock"

def log(msg):
    ts = datetime.datetime.now().strftime("%H:%M:%S.%f")[:-3]
    print(f"[{ts}] {msg}", flush=True)

def log_requests(data):
    """Extract and log HTTP request lines from data"""
    try:
        text = data.decode('utf-8', errors='replace')
        lines = text.split('\r\n')
        for line in lines:
            if line.startswith(('GET ', 'POST ', 'PUT ', 'DELETE ', 'HEAD ', 'OPTIONS ')):
                log(f"    REQ: {line}")
    except:
        pass

def forward_client_to_docker(src, dst, conn_id):
    total = 0
    buf = b''
    try:
        while True:
            data = src.recv(65536)
            if not data:
                break
            total += len(data)
            buf += data
            # Rewrite version in all occurrences
            rewritten = re.sub(rb'/v1\.\d+/', b'/v1.44/', buf)
            # Log HTTP request lines
            log_requests(data)
            dst.sendall(rewritten)
            buf = b''
    except Exception as e:
        pass
    finally:
        log(f"  C{conn_id}->D done ({total} bytes)")
        try: src.shutdown(socket.SHUT_WR)
        except: pass

def forward_docker_to_client(src, dst, conn_id):
    total = 0
    first_chunk = True
    try:
        while True:
            data = src.recv(65536)
            if not data:
                break
            total += len(data)
            if first_chunk:
                # Log response status
                first_line = data.split(b'\r\n')[0].decode('utf-8', errors='replace')
                log(f"  D->C{conn_id}: {first_line}")
                first_chunk = False
            dst.sendall(data)
    except Exception as e:
        pass
    finally:
        log(f"  D->C{conn_id} done ({total} bytes)")
        try: src.shutdown(socket.SHUT_WR)
        except: pass

def handle_client(client_sock, conn_id):
    log(f"CONN {conn_id} accepted")
    docker_sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    try:
        docker_sock.connect(DOCKER_SOCKET)
        t1 = threading.Thread(target=forward_client_to_docker, args=(client_sock, docker_sock, conn_id))
        t2 = threading.Thread(target=forward_docker_to_client, args=(docker_sock, client_sock, conn_id))
        t1.daemon = True
        t2.daemon = True
        t1.start()
        t2.start()
        t1.join()
        t2.join()
    except Exception as e:
        log(f"CONN {conn_id} ERR: {e}")
    finally:
        try: client_sock.close()
        except: pass
        try: docker_sock.close()
        except: pass
    log(f"CONN {conn_id} closed")

if os.path.exists(PROXY_SOCKET):
    os.remove(PROXY_SOCKET)

server = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
server.bind(PROXY_SOCKET)
os.chmod(PROXY_SOCKET, 0o777)
server.listen(200)
log(f"Proxy v5 on {PROXY_SOCKET}")

counter = [0]
signal.signal(signal.SIGTERM, lambda s, f: sys.exit(0))

while True:
    try:
        cs, _ = server.accept()
        counter[0] += 1
        t = threading.Thread(target=handle_client, args=(cs, counter[0]))
        t.daemon = True
        t.start()
    except: break
