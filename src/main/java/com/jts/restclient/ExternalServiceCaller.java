package com.jts.restclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class ExternalServiceCaller {
	
	private final RestClient restClient;
	
	public ExternalServiceCaller(RestClient.Builder restBuilder, @Value("${user.service.url}") String baseUrl) {
		this.restClient = restBuilder.baseUrl(baseUrl).build();
	}
	
	@PostMapping("createUser")
	ResponseEntity<Void> createUser(@RequestBody User user) {
		return restClient.post()
		.uri("/createUser")
		.contentType(MediaType.APPLICATION_JSON)
		.body(user)
		.retrieve()
		.toBodilessEntity();
	}
	
	@GetMapping("/findById/{id}")
	User getUser(@PathVariable String id) {
		return restClient.get()
		.uri("/findById/{id}", id)
		.accept(MediaType.APPLICATION_JSON)
		.retrieve()
		.body(User.class);
	}
	
	@GetMapping("/entityFindById/{id}")
	ResponseEntity<User> entityFindById(@PathVariable String id) {
		ResponseEntity<User> user =  restClient.get()
		.uri("/findById/{id}", id)
		.accept(MediaType.APPLICATION_JSON)
		.retrieve()
		.toEntity(User.class);
		
		System.out.println(user.getStatusCode());
		
		return user;
	}
	
	@GetMapping("/user-not-found/{id}")
	User userNotFound(@PathVariable String id) {
		return restClient.get()
		.uri("/findById/{id}", id)
		.accept(MediaType.APPLICATION_JSON)
		.retrieve()
		.onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
			throw new CustomException();
		}))
		.body(User.class);
	}
	
	
	@GetMapping("/userWithExchange/{id}")
	User userWithExchange(@PathVariable String id) {
		return restClient.get()
		.uri("/findById/{id}", id)
		.accept(MediaType.APPLICATION_JSON)
		.exchange((request, response) -> {
			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new CustomException();
			}
			
			ObjectMapper object = new ObjectMapper();
			User user = object.readValue(response.getBody(), User.class);
			
			return user;
		});
		
	}
	
	class CustomException extends RuntimeException {
		
		private static final long serialVersionUID = -5741490213721743756L;

		public CustomException() {
			super("User not found");
		}
	}
}
