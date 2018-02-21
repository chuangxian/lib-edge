package myzd.api.controllers;

import io.swagger.annotations.ApiParam;
import libedge.annotations.RateLimit;
import libedge.domain.request.ResultWrapper;
import myzd.api.domain.User;
import myzd.api.domain.UserInfo;
import org.springframework.web.bind.annotation.*;

/**
 * @author yrw
 * @since 2018/2/21
 */

@RestController
public class DemoRateLimitController {

	@RateLimit(rate = 5)
	@GetMapping("/api/v1/user/{id}/ratelimit")
	public ResultWrapper<User> getUser(
					@ApiParam(value = "User id")
					@PathVariable("id") Long id) {
		return new ResultWrapper<>(new User() {{
			setId(id);
			setName(String.format("Name of %d", id));
		}});
	}

	@RateLimit(rate = 10)
	@PostMapping("/api/v1/user/ratelimit")
	public ResultWrapper<User> createUser(
					@ApiParam(value = "User to be created") @RequestBody UserInfo user
	) {
		return new ResultWrapper<>(new User() {{
			setName(user.getName());
			setId(System.currentTimeMillis());
		}});
	}
}
