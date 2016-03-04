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
                    './../webapp/resources/fonts',
                    './../webapp/resources/css',
                    './../webapp/resources/js',
                    './../webapp/resources/images',
                    './../webapp/resources/videos'
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
                        './sass/exmatrikulator.sass',
                    './build/certificate.css':
                        './sass/certificate.sass'
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
            }
        },
        cssmin: {
            css: {
                files: {
                    './../webapp/resources/css/vendors.min.css': [
                        './build/vendors.css'
                    ],
                    './../webapp/resources/css/exmatrikulator.min.css': [
                        './build/exmatrikulator.css'
                    ],
                    './../webapp/resources/css/certificate.min.css': [
                        './build/certificate.css'
                    ]
                }
            }
        },
        copy: {
            roboto: {
                expand: true,
                src: './vendors/roboto-fontface/fonts/*',
                dest: './../webapp/resources/fonts/',
                flatten: true,
                filter: 'isFile'
            },
            fontawesome: {
                expand: true,
                src: './vendors/font-awesome/fonts/*',
                dest: './../webapp/resources/fonts/',
                flatten: true,
                filter: 'isFile'
            },
            img: {
                expand: true,
                src: './img/*',
                dest: './../webapp/resources/images/',
                flatten: true,
                filter: 'isFile'
            },
            videos: {
                expand: true,
                src: './videos/*',
                dest: './../webapp/resources/videos/',
                flatten: true,
                filter: 'isFile'
            },
            primefacesTheme: {
                expand: true,
                src: './primefaces/*',
                dest: './../webapp/resources/primefaces-exmatrikulator/',
                flatten: true,
                filter: 'isFile'
            },
            primefacesImages: {
                expand: true,
                src: './primefaces/images/*',
                dest: './../webapp/resources/primefaces-exmatrikulator/images/',
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
                    './../webapp/resources/js/exmatrikulator.min.js': [
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
                        src: ['./../webapp/resources/js/*.js'],
                        dest: '.',
                        ext: '.gz.js'
                    },
                    {
                        expand: true,
                        src: ['./../webapp/resources/css/*.css'],
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

    grunt.registerTask(
        'dev',
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
            'compress'
        ]
    );
};