import axios from "axios";

function mounted () {
    axios
        .get(`images`)
        .then((response) => {
        // JSON responses are automatically parsed.
        this.response = response.data;
        })
        .catch((e) => {
        this.errors.push(e);
        });
}

function getImg (id, name) {  //This method is used to get the image from backend
    var imageEl = document.getElementById(name);

    axios.get(`/images/`+id, { responseType:"blob" })
    .then(function (response) {
        var reader = new window.FileReader();
        reader.readAsDataURL(response.data);
        reader.onload = function() {
        var imageDataUrl = reader.result;
        imageEl.setAttribute("src", imageDataUrl);
        }
    });
};

function showImg (id, name) { //This method is used when we first load the page to show images as gallery
    this.$nextTick(function () { //Wait for DOM to be ready
        this.response.forEach( (img) => getImg(img.id, img.name.substr(0, img.name.lastIndexOf('.')).replace(/\s+/g, '')));
    })
};

async function deleteImage (id) { //I added this method to delete images from the server
    if (id==null) {
        alert("Please select an image!");
        return;
    }
    await axios.delete(`/images/`+id)
    .then(function(){
        alert('Image deleted successfully!');
    })
    .catch(function(){
        alert('Failed to delete image!');
    });
    location.reload();
};

async function setMeta (selected) { //Helper
  var meta = document.getElementById("meta");
  var type = document.getElementById("type");
  var size = document.getElementById("size")
  var id = document.getElementById("id")
  id.innerHTML = "Id : "+JSON.parse(JSON.stringify(selected)).id;
  type.innerHTML = "Type : "+JSON.parse(JSON.stringify(selected)).type;
  size.innerHTML = "Size : "+JSON.parse(JSON.stringify(selected)).size;
  console.log(meta.style.display);
  if (meta.style.display == "block") {
    meta.style.display = "none";
    return;
  }
  if (meta.style.display == "none")
    meta.style.display = "block";
  if (!meta.hasAttribute("display")) {
    meta.style.display = "block";
  }
}


function hideImg (name) { //This method is used by buttons to show an image if it's hidden and vice-versa
    var imageEl = document.getElementById(name);

    if (!imageEl.src) {
      alert("Please select an image!");
      return;
    }

    if (imageEl.style.display == "block") {
      imageEl.style.display = "none";
      return;
    }
    if (!imageEl.hasAttribute("display")) {
      imageEl.style.display = "block";
    }
};

function LoadGallery () { //This method is used by the button Hide/Show images as gallery
    this.response.forEach( (img) => this.hideImg(img.name.substr(0, img.name.lastIndexOf('.')).replace(/\s+/g, '')));
};

function downloadImage (id) { //This method is used to download images locally
    if (id==null) {
        alert("Please select an image!");
    }
    var a = document.createElement("a");
    a.href = '/images/' + id;
    a.download = id+".jpg";
    a.click();
}

function dataURLtoFile(dataurl, filename) { //Helper
  var arr = dataurl.split(','),
      mime = arr[0].match(/:(.*?);/)[1],
      bstr = atob(arr[1]),
      n = bstr.length,
      u8arr = new Uint8Array(n);

  while(n--){
      u8arr[n] = bstr.charCodeAt(n);
  }

  return new File([u8arr], filename+".jpg", {type:mime});
}

function treatImg (traitement, id, param1, param2) {  //This method is used to get the image from backend
  var imageEl = document.getElementById("myImg");
  let link = "";
  if(param2 == null){
    link = '/images/' +id+ "?"+ "algorithm=" +traitement+ "&p1=" +param1;
    if (param1 == null)
      link = '/images/' +id+ "?"+ "algorithm=" +traitement;
  }

  else {
    link = '/images/'+id+"?"+"algorithm="+traitement+"&p1=" +param1+ "&p2=" +param2;
  }

  axios.get(link, { responseType:"blob" }).then(function (response) {
    var reader = new window.FileReader();
    reader.readAsDataURL(response.data);
    reader.onload = function() {
    var imageDataUrl = reader.result;
    console.log(imageDataUrl);
    imageEl.setAttribute("src", imageDataUrl);
    let name = id+"-"+traitement+"-"+param1;
    var file = dataURLtoFile(imageDataUrl, id+"-"+traitement+"-"+param1);
    let formData = new FormData();
    formData.append('file', file);
    submitFile(formData, name);
    }
});

};

async function submitFile (formData, name){ //This method is used for uploads
    let skip = true;
    if (formData==null) {
      skip = false;
      let FORMDATA = new FormData();
      FORMDATA.append('file', this.file);
      formData = FORMDATA;
    }
    await axios.post( '/images',
        formData,
        {
          headers: {
              'Content-Type': 'multipart/form-data'
          }
        }
      ).then(function(){

      })
      .catch(function(){
        alert('FAILURE!!');
      });

    if (!skip) { //To clear filename from input
      this.$refs.file.value = null;
      alert("Image uploaded!")

    }
    if (skip) {
      alert("Image processed! To download or view it, please refresh and select the image: "+name+".");
    }
    location.reload();
    document.getElementById("reload").click();

};

function handleFileUpload() { //This method is a helper for the one above ^
    this.file = this.$refs.file.files[0];
};

function fbs_click(id) {
  var url = document.location.href+id;
  window.open('http://www.facebook.com/sharer.php?u='+encodeURIComponent(url),
    'sharer',
    'toolbar=0,status=0,width=626,height=436');

}


export { hideImg, LoadGallery, downloadImage, getImg, showImg, submitFile, deleteImage, handleFileUpload, treatImg, setMeta, mounted, fbs_click }
