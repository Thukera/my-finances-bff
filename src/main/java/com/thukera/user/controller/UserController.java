package com.thukera.user.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thukera.root.model.messages.NotFoundException;
import com.thukera.user.dto.UserDTO;
import com.thukera.user.model.entities.Role;
import com.thukera.user.model.entities.User;
import com.thukera.user.model.enums.RoleName;
import com.thukera.user.model.forms.LoginForm;
import com.thukera.user.model.forms.SignUpForm;
import com.thukera.user.repository.RoleRepository;
import com.thukera.user.repository.UserRepository;
import com.google.gson.Gson;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

	private static final Logger logger = LogManager.getLogger(AuthRestAPIs.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	Gson gson = new Gson();

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	RoleRepository roleRepository;

	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?>  listar() {
		
		logger.debug("######## ### FIND ALL ### ########");
		
		try {
			
			List<UserDTO> users = userRepository.findAll()
	                .stream()
	                .map(UserDTO::fromEntity)
	                .toList();

	        return ResponseEntity.ok(users);
			
		} catch (Exception e) {
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Internal Server Error");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> findById(@PathVariable Long id) throws Exception {

		logger.debug("######## ### FIND BY ID");

		try {

		    User user = userRepository.findById(id)
		            .orElseThrow(() -> new NotFoundException("Recurso não encontrado"));

		    return ResponseEntity.ok(UserDTO.fromEntity(user));

		} catch (NotFoundException e) {
			logger.debug("## NotFoundException Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Não encontrado");
			return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
			
		}  catch (Exception e) {
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Internal Server Error");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/{userid}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<?> updateUser(@PathVariable("userid") String userid, @RequestBody SignUpForm user) {

		logger.debug("######## ### UPDATE USER BY ID");
		
		try {
			User usuario = userRepository.getById(Long.parseLong(userid));

			usuario.setDoc(user.getDoc());
			usuario.setName(user.getName());
			usuario.setUsername(user.getUsername());
			usuario.setEmail(user.getEmail());
			usuario.setPassword(encoder.encode(user.getPassword()));
			usuario.setStatus(user.getStatus());

			Set<String> strRoles = user.getRole();
			Set<Role> roles = new HashSet<>();

			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
					roles.add(adminRole);

					break;
				case "pm":
					Role pmRole = roleRepository.findByName(RoleName.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
					roles.add(pmRole);

					break;
				default:
					Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
					roles.add(userRole);
				}
			});

			usuario.setRoles(roles);

			
			userRepository.save(usuario);

			Map<String, String> body = new HashMap<>();
			body.put("message", "Usuer Updated Sucessfuly");
			return ResponseEntity.ok(body);
			
		} catch (Exception e) {
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Internal Server Error");
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/changepassword")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public String resetPassword(@RequestBody LoginForm user) {
		
		logger.debug("######## ### CHANGE PASSWORD");
		

		User usuario = userRepository.findByEmail(user.getUsername());
		if (usuario == null) {
			throw new NotFoundException("Recurso não encontrado");
		} else {
			usuario.setPassword(encoder.encode(user.getPassword()));
		}
		userRepository.save(usuario);
		return gson.toJson("OK");
	}

	@PutMapping("/forgotpassword")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public String forgotPassword(@RequestBody SignUpForm user) {
		
		logger.debug("######## ### FORGOT PASSWORD");

		User usuario = userRepository.findByEmail(user.getEmail());
		if (usuario == null) {
			throw new NotFoundException("Recurso não encontrado");
		} else {

			//

			//

		}
		return gson.toJson("OK");
	}

	@PutMapping("/updateUser")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> updateUser(@RequestBody SignUpForm user) {
		
		logger.debug("######## ### UPDATE USER");

		try {

			User usuario = user.toModel();

			usuario.setId(user.getId());

			Set<String> strRoles = user.getRole();
			Set<Role> roles = new HashSet<>();

			strRoles.forEach(role -> {

				Role adminRole = roleRepository.findByName(RoleName.getRole(role))
						.orElseThrow(() -> new RuntimeException("Fail! -> Cause: " + role + " Role não encontrada."));
				roles.add(adminRole);

			});
			usuario.setRoles(roles);
			userRepository.save(usuario);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Error! User " + user.getUsername() + " not updated!");
		}
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/deleteUser/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		
		logger.debug("######## ### DELETE USER BY ID");

		try {
			userRepository.deleteById(id);
			
			Map<String, String> body = new HashMap<>();
			body.put("message", "User Deleted!");	
			return ResponseEntity.ok(body);

		} catch (NotFoundException e) {
			logger.debug("## NotFoundException Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Não encontrado");
			return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
			
		} catch (Exception e) {
			logger.debug("## General Exception");
			logger.error("### Exception : " + e.getClass());
			logger.error("### Message : " + e.getMessage());
			Map<String, String> body = new HashMap<>();
			body.put("message", "Error! User not deleted!");
			return ResponseEntity.badRequest().body("Error! User not deleted!");
			//return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
