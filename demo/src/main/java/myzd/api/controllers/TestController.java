package myzd.api.controllers;

import io.swagger.annotations.ApiParam;
import myzd.annotations.PipeConfig;
import myzd.annotations.SetHeaders;
import myzd.api.domain.Test1;
import myzd.domain.request.ListResult;
import myzd.domain.request.PagedResult;
import myzd.domain.request.ResultWrapper;
import myzd.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

/**
 * @author yrw
 * @since 2/10/2018
 */
@PipeConfig(clientHost = "${test.service.host}")
@RestController("/api/v1")
public class TestController {

	@Autowired
	private TestMapper testMapper;

	@PipeConfig
	@PreAuthorize("hasAuthority('15')")
	@GetMapping("/test/{id}")
	public ResultWrapper<Test1> getController(
					@ApiParam(value = "test1")
					@PathVariable("id") Long id
	) {
		return null;
	}

	@PipeConfig
	@PostMapping("/test")
	public ResultWrapper<Test1> postController(
					@ApiParam(value = "test2") @RequestBody @Valid Test1 test1,
					BindingResult result
	) {
		return null;
	}

	@PipeConfig
	@PutMapping("/test/{id}")
	public ResultWrapper<Test1> putController(
					@ApiParam(value = "test1") @PathVariable("id") Long id,
					@ApiParam(value = "test2") @RequestBody Test1 test1
	) {
		return null;
	}

	@PipeConfig
	@GetMapping("/test/re/{id}")
	public ResultWrapper<Test1> redirectController(
					@ApiParam(value = "test1") @PathVariable("id") Long id,
					HttpServletResponse response) throws IOException {
		return null;
	}

	@PipeConfig
	@GetMapping("/test/download")
	@SetHeaders(value = {"Content-Type:text/jpg",
					"Content-Disposition:attachment;filename=1.jpg"})
	public void fileDownload(HttpServletResponse response)
					throws MalformedURLException, IOException {
		return;
	}

	@PipeConfig
	@GetMapping("/test/page/pojo")
	public ResultWrapper<PagedResult<Test1>> pageTest() {
		return null;
	}

	@PipeConfig
	@GetMapping("/test/page/list")
	public ResultWrapper<PagedResult<List<Object>>> pageListTest() {
		return null;
	}

	@PipeConfig
	@GetMapping("/test/list")
	public ResultWrapper<ListResult<Test1>> listTest() {
		return null;
	}

	@PipeConfig
	@GetMapping("/test/list/map")
	public ResultWrapper<ListResult<HashMap<String, String>>> listMapTest() {
		return null;
	}

	@GetMapping("/test/dateSource")
	@PreAuthorize("hasAuthority('99')")
	public ResultWrapper<String> dateSourceTest() {
		return new ResultWrapper<String>(200, "message", testMapper.selectMethod());
	}

	@PipeConfig
	@GetMapping("/test/empty")
	public ResultWrapper<String> emptyDataTest() {
		return null;
	}
}
