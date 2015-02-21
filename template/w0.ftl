<!DOCTYPE html>
<html>
<head>
    <title>qcharge</title>
    <link href="/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <img src='/img/qcharge.png' style="max-width: 100%; margin: auto"/>

    <form action="info_aadress" method="get">
        <input class="btn btn-success btn-block" type="submit" value="INFO"
               name="Submit" id="to_info"/>
    </form>
    <a class="btn btn-success btn-block" id="to_charge" href="/payment/start">CHARGE</a>
</div>
</body>
</html>