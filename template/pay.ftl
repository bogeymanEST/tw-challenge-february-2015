<!DOCTYPE html>
<html>
<head>
    <title>Receipt</title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body>
<div class="container">
    <form action="/payment/process/${paymentId}" method="POST">
        <script
                src="https://checkout.stripe.com/checkout.js" class="stripe-button"
                data-key="pk_test_V0y72jZH8AKqn0GXQCmb5urU"
                data-amount="${price?c}"
                data-name="Taxi Co (via qcharge)"
                data-description="Taxi fare ${pprice}€">
        </script>
    </form>
</div>
<script type="text/javascript" src="https://js.stripe.com/v2/"></script>
</body>
</html>