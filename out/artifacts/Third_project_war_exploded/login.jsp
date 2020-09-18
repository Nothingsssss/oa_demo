<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>

<html>
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>myoa登录</title>
<link rel="stylesheet" href="static/css/bootstrap.min.css">
<link rel="stylesheet" href="static/css/style1.css">
<link rel="stylesheet" href="static/css/bootstrapValidator.min.css">
<script src="static/js/jquery-1.11.0.min.js"></script>
<script src="static/js/bootstrap.min.js"></script>
<script src="static/js/bootstrapValidator.min.js"></script>
</head>
<body onkeydown="keyLogin()">

	<div class="container">
		<div class="row">
			<div class="col-md-offset-3 col-md-6">
				
				<form class="form-horizontal" action="login" method="post" id="loginForm">
					<span class="heading">欢迎登录办公系统</span>
					<div class="form-group">
						<div class="col-sm-10">
						<input type="text" name="username" class="form-control" id="username"
							placeholder="请输入用户名" > <i class="fa fa-user"></i>
						</div>
					</div>
					<div class="form-group">
						<div class="col-sm-10">
						<input type="password" name="password" class="form-control" id="password"
							placeholder="请输入密码" > <i class="fa fa-lock"></i> <a href="#"
							class="fa fa-question-circle"></a>
						</div>
					</div>
					<div class="form-group">
						 
						<div class="main-checkbox">
							<input type="checkbox" onblur="checkValidity()" name="remember" value="true" id="checkbox1" />
							<label for="checkbox1"></label>
						</div>
						
						<span class="text">Remember me</span>
						<div class="error">${errorMsg}</div>
						<div class="col-sm-10">
							<%--data-target="#createUserModal"--%>
							<button type="button" class="btn btn-primary" onclick="checklogin()" title="登录" data-toggle="modal">登录</button>
						</div>
					</div>
					<div class="modal fade" id="createUserModal" tabindex="-1" role="dialog"
						 aria-labelledby="myModalLabel" aria-hidden="true">
						<div class="modal-dialog">
							<div class="modal-content">
								<div class="modal-header">
									<button type="button" class="close" data-dismiss="modal"
											aria-hidden="true">×</button>
									<h3 id="myModalLabel">请输入验证码</h3>
								</div>
								<div class="modal-body">
									<table class="table table-bordered table-striped" width="220px">
										<tr>
											<td class="center">
												<div class="text-center">
												<img src="${pageContext.request.contextPath}/checkCode" style="width: 100%;height: 100px" alt="验证码" id="check">
												</div>
											</td>
										</tr>
										<tr>
											<td><input class="form-control" type="text" id="mycode" name="validateCode" placeholder="验证码"></td>
										</tr>
									</table>
								</div>
								<div class="modal-footer">
									<button type="submit" class="btn btn-success" data-dismiss="modal"
											aria-hidden="true" onclick="login()">登录</button>
								</div>
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>
	</div>

	<script>
        window.onload = function () {
            document.getElementById("check").onclick=function() {
                this.src="${pageContext.request.contextPath}/checkCode?random="+Math.random();
            }
        }

        function keyLogin() {
            if(event.keyCode == 13) {
                event.returnValue=false;
                event.cancel = true;
                checklogin();
			}
		}

        //服务端校验
		function checklogin() {
            var flag = true;
            if ($("#username").val() == "" || $("#password").val() == "") {
                alert("请填写用户名或密码");
                flag = false;
            }
            if (flag){
				$("#createUserModal").modal('show')
			}
        }
        
        function login() {
			if($("#mycode").val() == ""){
                alert("请填写验证码");
			}else {
                document.getElementById('loginForm').submit();
			}
        }
        
        $(document).ready(function(){
            $("#mycode").blur(function () {
                if ($("#mycode").val() == "") {
                    alert("请填写验证码");
                }
            });
        })

	</script>
</body>
</html>