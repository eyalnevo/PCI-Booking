if (window.addEventListener) {
	window.addEventListener('message', handleValidateMessageFromPciBooking);
}
else if (window.attachEvent) {
	window.attachEvent('onmessage', handleValidateMessageFromPciBooking);
}
function handleValidateMessageFromPciBooking(e) {
    if (e.origin.toLowerCase() === "https://service.pcibooking.net/") {
        if (e.data == 'valid') {
            // Card form is valid - continue with user flow
        } else {
            // Card form is not valid
        }
    }
}
