package myzd.api.controllers;

import io.swagger.annotations.ApiParam;
import libedge.annotations.Authentication;
import libedge.annotations.PipeConfig;
import libedge.domain.request.ResultWrapper;
import myzd.api.domain.User;
import myzd.api.domain.UserInfo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;

/**
 * @author yrw
 * @since 2018/2/21
 */

@RestController
public class DemoAuthorizationController {

	@PipeConfig(clientHost = "${test.service.host}")
	@Authentication(envEncryption = "${source.intranet.secret}")
	@GetMapping("/api/v1/user/{id}/authenticate")
	public ResultWrapper<User> authenticateUser(
					@ApiParam(value = "User id")
					@PathVariable("id") Long id
	) {
		return null;
	}

	@Authentication
	@PreAuthorize("hasAuthority('99')")
	@GetMapping("/api/v1/user/{id}/authorize")
	public ResultWrapper<User> getUser(
					@ApiParam(value = "User id")
					@PathVariable("id") @Max(value = 100) Long id
	) {
		return new ResultWrapper<User>() {{
			setCode(1000000);
			setMessage("OK");
			setData(new User() {{
				setId(id);
				setName("Name of " + id);
			}});
		}};
	}

	@Authentication
	@PreAuthorize("hasAuthority('99')")
	@PostMapping("/api/v1/user/authorize")
	public ResultWrapper<User> createUser(
					@ApiParam(value = "User to be created") @RequestBody UserInfo user
	) {
		return new ResultWrapper<User>(new User() {{
			setName(user.getName());
			setId(System.currentTimeMillis());
		}});
	}

}