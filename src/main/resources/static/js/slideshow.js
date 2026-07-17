let slideIndex = 0;
let slideInterval;

function showSlides() {
    const slides = document.getElementsByClassName("mySlides");

    for (let i = 0; i < slides.length; i++) {
        slides[i].style.display = "none";
    }

    slideIndex++;

    if (slideIndex > slides.length) {
        slideIndex = 1;
    }

    slides[slideIndex - 1].style.display = "block";

    slideInterval = setTimeout(showSlides, 9000);
}

function changeSlide(n) {
    clearTimeout(slideInterval);

    const slides = document.getElementsByClassName("mySlides");

    slideIndex += n;

    if (slideIndex > slides.length) {
        slideIndex = 1;
    }

    if (slideIndex < 1) {
        slideIndex = slides.length;
    }

    for (let i = 0; i < slides.length; i++) {
        slides[i].style.display = "none";
    }

    slides[slideIndex - 1].style.display = "block";

    slideInterval = setTimeout(showSlides, 9000);
}

showSlides();