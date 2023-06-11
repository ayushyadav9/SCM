const toggleSideBar = () => {
  if ($(".sidebar").is(":visible")) {
    //close sidebar
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "1.5rem");
    $(".fas").css("display", "block");
  } else {
    //open sidebar
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "20%");
    $(".fas").css("display", "none");
  }
};

const search = () => {
  let query = $("#search-input").val();
  if (query == "") {
    $(".search-result").hide();
  } else {
    let url = `http://localhost:8080/search/${query}`;

    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        let text = `<div class='list-group'>`;

        data.forEach((con) => {
          text += `<a href="/user/contact/${con.cid}" class='list-group-item list-group-item-action'> ${con.name} </a>`;
        });

        text += `</div>`;
        $(".search-result").html(text);
        $(".search-result").show();
      });
  }
};

const paymentStart = () => {
  let amount = 100;
  $.ajax({
    url: "/user/create_order",
    data: JSON.stringify({ amount: amount, info: "order_request" }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (res) {
      console.log(res);
      if (res.status == "created") {
        var options = {
          key: "rzp_test_V8DGMsJKerCd7z",
          amount: res.amount,
          currency: "INR",
          name: "Smar Contact Manager",
          description: "Donation",
          order_id: res.id,
          handler: function (response) {
            
            updatePaymentOnServer(response.razorpay_payment_id, response.razorpay_order_id, "paid")
          },
          notes: {address: "Ayush Yadav"},
          theme: {color: "#3399cc"}
        };
        let rzp = new Razorpay(options)
        rzp.on('payment.failed', function (response){
          console.log(response);
          swal("Payment Failed!", "Oops payment failed", "error");
        });
        rzp.open()
      }else{
        swal("Payment Failed!", "Not able to create request. Try again", "error");
      }
    },
    error: function (err) {
      console.log(err);
      swal("Payment Failed!", "Something went wrong", "error");
    },
  });
};


const updatePaymentOnServer = (p_id,o_id,status)=>{
  $.ajax({
    url: "/user/update_order",
    data: JSON.stringify({ payment_id: p_id, order_id: o_id, status:status }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (res) { swal("Payment Succesful!", "Thank you for your donation", "success"); },
    error: function (err) { swal("Payment Success!", "Payment is success, but we unable to save the the order in our databse", "error");}
  })
}