# AlienInviders 2022

## Overview

This is a rework of my old game I made back when I was in highschool.
The game is an AI simulator with elements of space fighting.

Currently the game is being developed in Rust and WGPU with no WASM target compilation.

## Building & running

It is as simple as running `cargo build` from the project root.
You'd need Rust 1.66+ for this.

Running the game is as simple as `cargo run` from the project root.

Since WGPU supports OpenGL, DX11, DX12 and Vulkan, most modern hardware (since back 2017-ish) should be supported.
