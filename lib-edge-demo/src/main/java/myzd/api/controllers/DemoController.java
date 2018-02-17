package myzd.api.controllers;

import io.swagger.annotations.ApiParam;
import libedge.annotations.Authentication;
import libedge.annotations.PipeConfig;
import libedge.annotations.RateLimit;
import libedge.annotations.SetHeaders;
import libedge.domain.exceptions.GenericException;
import libedge.domain.request.ListResult;
import libedge.domain.request.PagedResult;
import libedge.domain.request.ResultWrapper;
import myzd.api.domain.User;
import myzd.api.domain.UserInfo;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import java.io.IOException;
import java.util.List;
import java.util.Map;

//import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author yrw
 * @since 2/10/2018
 */

@Validated
@PipeConfig(clientHost = "${test.service.host}")
@RestController
@RateLimit(rate = 5)
public class DemoController {

	@PipeConfig
	@RateLimit
	@GetMapping("/api/v1/user/{id}")
	public ResultWrapper<User> getUser(
					@ApiParam(value = "User id")
					@PathVariable("id") @Max(value = 100) Long id) {
		return null;
	}

	@PipeConfig
	@PostMapping("/api/v1/user")
	public ResultWrapper<User> createUser(
					@ApiParam(value = "User to be created") @RequestBody @Valid UserInfo user,
					BindingResult result) {
		return null;
	}

	@PipeConfig
	@PutMapping("/api/v1/user/{id}")
	public ResultWrapper<User> updateUser(
					@ApiParam(value = "User id") @PathVariable("id") Long id,
					@ApiParam(value = "Latest user info") @RequestBody UserInfo user
	) {
		return null;
	}

	@GetMapping("/api/v1/redirect")
	public void mockRedirect(
					@ApiParam(value = "The url to be redirected to") @RequestParam("url") String url,
					HttpServletResponse response) throws IOException {
		response.sendRedirect(url);
	}

	@PipeConfig
	@GetMapping("/api/v1/test/download")
	@SetHeaders(value = {"Content-Type:image/png",
					"Content-Disposition:attachment;filename=baidu.png"})
	public void fileDownload(HttpServletResponse response) {
	}

	@PipeConfig
	@GetMapping("/api/v1/test/page")
	public ResultWrapper<PagedResult<String>> getPage() {
		return null;
	}

	@PipeConfig
	@GetMapping("/api/v1/test/page/list")
	public ResultWrapper<PagedResult<List<String>>> getPageList() {
		return null;
	}

	@PipeConfig
	@GetMapping("/api/v1/test/list")
	public ResultWrapper<ListResult<User>> getList() {
		return null;
	}

	@PipeConfig
	@GetMapping("/api/v1/test/list/map")
	public ResultWrapper<ListResult<Map<String, String>>> getListMap() {
		return null;
	}

	@PipeConfig
	@GetMapping("/api/v1/servererror")
	public ResultWrapper<String> serverError(
					@RequestParam("code") String code,
					@RequestParam("message") String message
	) throws GenericException {
		throw new GenericException(code, message);
	}

	@Authentication
	@GetMapping("/api/v1/authenticate")
	public ResultWrapper<String> authenticateUser() {
		return new ResultWrapper<String>() {{
			setCode(1000000);
			setMessage("OK");
			setData("success authenticated");
		}};
	}

//	@Authentication
//	@PreAuthorize("hasAuthority('99')")
//	@PostMapping("/api/v1/authorize")
//	@PipeConfig
//	public ResultWrapper<UserInfo> authorizeUser(@RequestBody UserInfo user) {
//		return null;
//	}
}
