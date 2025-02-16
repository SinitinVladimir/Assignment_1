import socket
import threading

def start_tcp_server(port):
    """Start a TCP server."""
    def server_thread():
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(("0.0.0.0", port))
        server_socket.listen(5)
        print(f"TCP Server listening on port {port}...")

        try:
            while True:
                client_socket, client_address = server_socket.accept()
                print(f"Connection from {client_address}")
                message = client_socket.recv(1024).decode()
                print(f"Received: {message}")

                # respond to the client
                client_socket.sendall("Message received by the TCP server.".encode())
                client_socket.close()
        except KeyboardInterrupt:
            print("TCP Server stopped.")
        finally:
            server_socket.close()

    threading.Thread(target=server_thread, daemon=True).start()


def start_udp_server(port):
    """Start a UDP server."""
    def server_thread():
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        server_socket.bind(("0.0.0.0", port))
        print(f"UDP Server listening on port {port}...")

        try:
            while True:
                message, client_address = server_socket.recvfrom(1024)
                print(f"Received: {message.decode()} from {client_address}")

                # Respond to the client
                server_socket.sendto("Message received by the UDP server.".encode(), client_address)
        except KeyboardInterrupt:
            print("UDP Server stopped.")
        finally:
            server_socket.close()

    threading.Thread(target=server_thread, daemon=True).start()


def start_tcp_client(ip, port, message):
    """Start a TCP client."""
    try:
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((ip, port))
        print(f"Connected to TCP server {ip}:{port}")
        # send message with newline
        client_socket.sendall((message + "\n").encode())
        print(f"Message sent to TCP server {ip}:{port}")
        client_socket.shutdown(socket.SHUT_WR)  # ensure the server processes the message
        print(f"Message is being processed by the TCP server {ip}:{port}")
        response = client_socket.recv(1024).decode()
        print(f"TCP Server responded: {response}")
    except Exception as e:
        print(f"Error connecting to TCP server: {e}")
    finally:
        client_socket.close()


def start_udp_client(ip, port, message):
    """Start a UDP client."""
    try:
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        server_address = (ip, port)

        # send message to the server
        client_socket.sendto(message.encode(), server_address)
        print(f"Message sent to UDP server {ip}:{port}: {message}")

        # Wait for a response
        response, server_address = client_socket.recvfrom(1024)
        print(f"UDP Server responded from {server_address}: {response.decode()}")

    except Exception as e:
        print(f"Error during UDP communication: {e}")
    finally:
        client_socket.close()


def main():
    while True:
        print("\nSelect mode: ")
        print("1. Start TCP Server")
        print("2. Start TCP Client")
        print("3. Start UDP Server")
        print("4. Start UDP Client")
        print("5. Exit")
        mode = input("Enter mode (1, 2, 3, 4, or 5): ").strip()

        if mode == "1":
            # TCP Server Mode
            try:
                port = int(input("Enter port to start the TCP server: ").strip())
                print("Starting TCP Server...")
                start_tcp_server(port)
            except ValueError:
                print("Invalid port number. Please try again.")
        elif mode == "2":
            # TCP Client Mode
            try:
                ip = input("Enter TCP server IP address: ").strip()
                port = int(input("Enter TCP server port: ").strip())
                message = input("Enter message to send to TCP server: ").strip()
                print("Starting TCP Client...")
                start_tcp_client(ip, port, message)
            except ValueError:
                print("Invalid input. Please check the IP, port, or message and try again.")
        elif mode == "3":
            # UDP Server Mode
            try:
                port = int(input("Enter port to start the UDP server: ").strip())
                print("Starting UDP Server...")
                start_udp_server(port)
            except ValueError:
                print("Invalid port number. Please try again.")
        elif mode == "4":
            # UDP Client Mode
            try:
                ip = input("Enter UDP server IP address: ").strip()
                port = int(input("Enter UDP server port: ").strip())
                message = input("Enter message to send to UDP server: ").strip()
                print("Starting UDP Client...")
                start_udp_client(ip, port, message)
            except ValueError:
                print("Invalid input. Please check the IP, port, or message and try again.")
        elif mode == "5":
            print("Exiting program.")
            break
        else:
            print("Invalid choice. Please try again.")


if __name__ == "__main__":
    main()

