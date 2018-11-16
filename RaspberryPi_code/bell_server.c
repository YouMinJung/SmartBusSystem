#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <wiringPi.h>

#define LED 7
#define BUFF_SIZE 1024

int main(void) {
	int server_socket;
	int client_socket;
	int client_addr_size;
	struct sockaddr_in server_addr;
	struct sockaddr_in client_addr;
	char buff_msg[BUFF_SIZE+5];

	if(wiringPiSetup() == -1)
		return 1;

	pinMode(LED, OUTPUT);
	digitalWrite(LED, 0);

	server_socket = socket(PF_INET, SOCK_STREAM, 0);
	if(server_socket == -1) {
		printf("server socket can't create");
		exit(1);
	}
	else
		printf("Socket created : %d\n", socket);

	memset(&server_addr, 0, sizeof(server_addr));

	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(7777);
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	if(bind(server_socket, (struct sockaddr*)&server_addr, sizeof(server_addr)) == -1)
		exit(1);
	else
		printf("socket binded\n");

	while(1) {
		if(listen(server_socket, 5) == -1)
			exit(1);
		else
			printf("listening...\n");

		client_addr_size = sizeof(client_addr);
		client_socket = accept(server_socket, (struct sockaddr*)&client_addr, &client_addr_size);
		if(client_socket == -1)
			exit(1);
		else
			printf("client socket created..\n");

		read(client_socket, buff_msg, strlen(buff_msg)-5);
		printf("receive: %s\n", buff_msg);

		// if receive bell message -> bright LED
		if(strstr(buff_msg, "bell")!=NULL) {
			// LED ON
			digitalWrite(LED, 1);

			// send out message
			char msg[] = "OUT";
			write(client_socket, msg, strlen(msg));
		}

		close(client_socket);
	}
}
