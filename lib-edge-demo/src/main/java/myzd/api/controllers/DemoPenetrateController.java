package myzd.api.controllers;

import io.swagger.annotations.ApiParam;
import libedge.annotations.PipeConfig;
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

/**
 * @author yrw
 * @since 2018/2/21
 */

@Validated
@PipeConfig(clientHost = "${test.service.host}")
@RestController
public class DemoPenetrateController {

	@PipeConfig
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

	//301/302

	@PipeConfig
	@GetMapping("/api/v1/redirect")
	public void mockRedirect(
					@ApiParam(value = "The url to be redirected to") @RequestParam("url") String url,
					HttpServletResponse response) throws IOException {
		response.sendRedirect(url);
	}

	//>=500

	@PipeConfig
	@GetMapping("/api/v1/servererror")
	public ResultWrapper<String> serverError(
					@RequestParam("code") String code,
					@RequestParam("message") String message
	) throws GenericException {
		throw new GenericException(code, message);
	}

	@PipeConfig
	@GetMapping("/api/v1/clienterror")
	public ResultWrapper<String> clientError() {
		return null;
	}

	@PipeConfig
	@GetMapping("/api/v1/test/download")
	@SetHeaders(value = {"Content-Type:image/png",
					"Content-Disposition:attachment;filename=baidu.png"})
	public void fileDownload(HttpServletResponse response) {
	}


	//type ResultWrapper/PageResult/ListResult

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

}
