/**
 * Created by matthias on 08.01.16.
 */
module.exports = function(grunt) {
    grunt.initConfig({
        clean: {
            options: {
                force: true
            },
            build: {
                src: [
                    './../resources/fonts',
                    './../resources/css',
                    './../resources/js'
                ]
            }
        },
        grunt: {
            buildsome: {
                gruntfile: 'vendors/bootstrap/Gruntfile.js',
                tasks: 'dist'
            }
        },
        less: {
            dist: {
                files: {
                    './build/bootstrap-nav-wizard.css':
                        './vendors/bootstrap-nav-wizard/bootstrap-nav-wizard.less'
                }
            }
        },
        sass: {
            options: {
                sourceMap: false
            },
            dist: {
                files: {
                    './build/roboto-fontface.css':
                        './vendors/roboto-fontface/css/roboto-fontface.scss',
                    './build/font-awesome.css':
                        './vendors/font-awesome/scss/font-awesome.scss',
                    './build/exmatrikulator.css':
                        './sass/exmatrikulator.sass'
                }
            }
        },
        coffee: {
            options: {
                bare: true
            },
            compile: {
                files: {
                    './build/exmatrikulator.js':
                        './coffee/exmatrikulator.coffee'
                }
            }
        },
        concat: {
            cssvendors: {
                src: [
                    './build/roboto-fontface.css',
                    './build/font-awesome.css',
                    './vendors/bootstrap/dist/css/bootstrap.css',
                    './build/bootstrap-nav-wizard.css'
                ],
                dest: './build/vendors.css'
            },
            jsvendors: {
                src: [
                    './vendors/jquery/dist/jquery.min.js',
                    './vendors/bootstrap/dist/js/bootstrap.min.js'
                ],
                dest: './../resources/js/vendors.min.js'
            }
        },
        cssmin: {
            css: {
                files: {
                    './../resources/css/vendors.min.css': [
                        './build/vendors.css'
                    ],
                    './../resources/css/exmatrikulator.min.css': [
                        './build/exmatrikulator.css'
                    ]
                }
            }
        },
        copy: {
            roboto: {
                expand: true,
                src: './vendors/roboto-fontface/fonts/*',
                dest: './../resources/fonts/',
                flatten: true,
                filter: 'isFile'
            },
            fontawesome: {
                expand: true,
                src: './vendors/font-awesome/fonts/*',
                dest: './../resources/fonts/',
                flatten: true,
                filter: 'isFile'
            }
        },
        uglify: {
            options: {
                mangle: false,
                screwIE8: true
            },
            js: {
                files: {
                    './../resources/js/exmatrikulator.min.js': [
                        './build/exmatrikulator.js'
                    ]
                }
            }
        },
        exec: {
            build_folder: 'rm -rf build'
        },
        compress: {
            main: {
                options: {
                    mode: 'gzip'
                },
                files: [
                    {
                        expand: true,
                        src: ['./../resources/js/*.js'],
                        dest: '.',
                        ext: '.gz.js'
                    },
                    {
                        expand: true,
                        src: ['./../resources/css/*.css'],
                        dest: '.',
                        ext: '.gz.css'
                    }
                ]
            }
        }
    });

    grunt.loadNpmTasks('grunt-grunt');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-coffee');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-compress');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.loadNpmTasks('grunt-sass');
    grunt.loadNpmTasks('grunt-exec');

    grunt.registerTask(
        'build',
        [
            'clean',
            'grunt',
            'sass',
            'less',
            'coffee',
            'concat',
            'copy',
            'cssmin',
            'uglify',
            'exec',
            'compress'
        ]
    );
};