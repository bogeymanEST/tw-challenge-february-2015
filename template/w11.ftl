<!DOCTYPE html>
<html>
<head>
    <title>Specify amount</title>
    <link href="/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <img style="max-width: 100%" src="/img/qcharge.png"/>
<br/>
    <label for="amount">Amount</label> <br>

    <div class="input-group">
        <input class="form-control" type="text" value="0" id="price">
        <div class="input-group-addon">€</div>
    </div>
    <br>
    <a class="btn btn-success btn-block form-control" type="submit" onclick="generateCode()">GENERATE QR CODE</a>
</div>
<script src="https://code.jquery.com/jquery-2.1.3.min.js"></script>
<script>
function generateCode() {
    $.get("/ajax/qr_code", {recipientId: "r_9K49REY6PK86YOOJBQ3FVZY7I", price: $("#price").val()}, function(data) {
        $(".container").html(data);
    });
}
var polling = true;
    function startPoll(id) {
        var i = setInterval(
                function() {
                    if(!polling) clearInterval(i);
                    $.getJSON("/api/payment/status",
                            {paymentId: id},
                            function(data) {
                                console.log(data.status);
                                if(data.status == 1)  {
                                    $.get("/ajax/payment_complete", function(data) {
                                        $(".container").html(data);
                                    });
                                    polling = false;
                                }
                            });
                },
                2000
        )

    }
</script>
</body>
</html>