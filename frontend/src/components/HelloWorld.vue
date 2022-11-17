<template>

  <div class="imagesOnServer">
    <h1>Images On Server</h1>

    <h3>Individual Images :</h3>
    <select v-model="selected" @click="getImg(JSON.stringify(selected).charAt(6), 'myImg');">
      <option v-for="img in response" v-bind:key="img.name" v-bind:value="img" >
        {{ img.name }}
      </option>
    </select><br/>

    <button id="hide" @click="hideImg('myImg'); setMeta(selected)">Hide/Show image</button><br/>
    <button id="download" @click="downloadImage(JSON.stringify(selected).charAt(6))">Download image locally</button><br/>
    <button id="delete" @click="deleteImage(JSON.stringify(selected).charAt(6))">Delete image</button>
    <div id="meta">
      <p><strong>Metadata : </strong></p>
      <h4 id="id"></h4>
      <h4 id="type"></h4>
      <h4 id="size"></h4>

    </div>

    <h3>Filter</h3>
    <select v-model="selection">
            <option value="" selected disabled>Please select one</option>
            <option>Luminosity</option>
            <option>Histogramme</option>
            <option>Blur</option>
            <option>Gaussian</option>
            <option>Contour</option>
            <option>Coloredfilter</option>
            <option>Glitch Vertical</option>
            <option>Glitch Horizontal</option>
            <option>negative</option>
    </select>&nbsp;
    <input v-model="param1" placeholder="paramètre 1">&nbsp;
    <input v-model="param2" placeholder="paramètre 2">&nbsp;

    <button id="apply" @click="treatImg(selection.toLowerCase(), JSON.stringify(selected).charAt(6), param1, param2)">Apply treatment</button><br/>

    <img id="myImg">

    <h3>Image Upload :</h3>
    <div class="container">
      <div>
        <label>File
          <input type="file" id="file" ref="file" v-on:change="handleFileUpload()"/>
        </label>
          <button v-on:click="submitFile()">Submit</button>
      </div>
    </div><br/>

    <h3>Images Gallery :</h3>
    <button @click="LoadGallery()">Hide/Show as gallery</button> <br/><br/>
    <button @click="fbs_click(JSON.stringify(selected).charAt(6))">Share on Facebook</button> <br/><br/>

    <div class="parent">
      <div class="gallery" v-for="img in response" v-bind:key="img.name">
          <img :id="img.name.substr(0, img.name.lastIndexOf('.')).replace(/\s+/g, '')">
      </div>
    </div>

    <template v-for="img in response"> {{ showImg(img.id, img.name.substr(0, img.name.lastIndexOf('.')).replace(/\s+/g, '')) }} </template>
    <a id="reload" @click="updateComponent()"></a>

  </div>

</template>

<script>
import { hideImg, LoadGallery, downloadImage, getImg, showImg, submitFile, deleteImage, handleFileUpload, treatImg, setMeta, mounted, fbs_click } from './http-api.js'
export default {
  name: "Images",
  props: {
    msg: String,
  },
  data() {
    return {
      response: [],
      errors: [],
      file: ''
    };
  },
  methods: {
    updateComponent (){
      this.$forceUpdate();
    },
    hideImg,
    setMeta,
    LoadGallery,
    downloadImage,
    getImg,
    showImg,
    deleteImage,
    submitFile,
    treatImg,
    handleFileUpload,
    fbs_click
  },
  mounted
};


</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}

div.parent {
  margin: 0px auto 0px auto;
  max-width: 70%;
}

div.gallery {
  display: inline-block;
  width: 400px;
}

div.gallery:hover {
  border: 3px solid #777;
  cursor: crosshair;
}

div.gallery img {
  display: none;
  border: 1px solid #021a40;
  width: 100%;
  height: 300px;
}

#myImg {
  display: none;
  margin: 10px auto -25px auto;
  height: 300px;
  width: 30%;
}

#hide {
  background: #407294;
}

#meta {
  display: none;
}

#delete {
  background: red;
  margin-bottom: -50px;
}

button {
  background: #222;
  height: 35px;
  min-width: 100px;
  border: none;
  border-radius: 10px;
  color: #eee;
  font-size: 20px;
  justify-content: center;
  cursor: pointer;
  margin-top: 2px;
}

a {
  color: #42b983;
}

</style>
