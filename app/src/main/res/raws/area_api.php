<?php
	header("Content-type:text/html;charset=utf-8");
	mysql_connect("localhost","root","")or die("服务器不存在");
	mysql_select_db("db_weather")or("数据库不存在");
	mysql_query("set names utf8");
		$sql = "select distinct area_name from tb_weather";
		$r = mysql_query($sql);
		$result = array();
		while($row = mysql_fetch_array($r)){
			$result[]=$row[0];
		}
		echo json_encode($result);
?>