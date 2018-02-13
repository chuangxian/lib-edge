package myzd.api.controllers;

import io.swagger.annotations.ApiParam;
import myzd.annotations.PipeConfig;
import myzd.annotations.SetHeaders;
import myzd.api.domain.User;
import myzd.api.domain.UserInfo;
import myzd.domain.exceptions.GenericException;
import myzd.domain.request.ListResult;
import myzd.domain.request.PagedResult;
import myzd.domain.request.ResultWrapper;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

/**
 * @author yrw
 * @since 2/10/2018
 */
@PipeConfig(clientHost = "http://127.0.0.1:${local.server.port}/mock/")
@RestController
public class TestController {

	@PipeConfig
	@GetMapping("/api/v1/user/{id}")
	public ResultWrapper<User> getUser(
		@ApiParam(value = "User id")
		@PathVariable("id") Long id
	) {
		return null;
	}

	@PipeConfig
	@PostMapping("/api/v1/user")
	public ResultWrapper<User> createUser(
		@ApiParam(value = "User to be created") @RequestBody @Valid UserInfo user,
		BindingResult result
	) throws GenericException {
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
	public void fileDownload(HttpServletResponse response)
		throws MalformedURLException, IOException {
	}

	@PipeConfig
	@GetMapping("/api/v1/test/page/pojo")
	public ResultWrapper<PagedResult<String>> pageTest() {
		return null;
	}

	@PipeConfig
	@GetMapping("/api/v1/test/page/list")
	public ResultWrapper<PagedResult<List<String>>> pageListTest() {
		return null;

	}

	@PipeConfig
	@GetMapping("/api/v1/test/list")
	public ResultWrapper<ListResult<User>> listTest() {
		return null;
	}

	@PipeConfig
	@GetMapping("/api/v1/test/list/map")
	public ResultWrapper<ListResult<Map<String, String>>> listMapTest() {
		return null;
	}
}
